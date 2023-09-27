package com.proselyteapi.dataprovider.controller;

import com.proselyteapi.dataprovider.dto.AuthRequestDto;
import com.proselyteapi.dataprovider.dto.AuthResponseDto;
import com.proselyteapi.dataprovider.dto.UserDto;
import com.proselyteapi.dataprovider.mapper.UserMapper;
import com.proselyteapi.dataprovider.service.AuthenticationService;
import com.proselyteapi.dataprovider.service.TokenService;
import com.proselyteapi.dataprovider.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthRestController {
    private final AuthenticationService authenticationService;
    private final TokenService tokenService;
    private final UserService userService;
    private final UserMapper userMapper;

    public static final String REGISTER_PATH = "/register";
    public static final String TOKEN_PATH = "/get-token";
    public static final String API_KEY_PATH = "/get-api-key";


    @PostMapping(REGISTER_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserDto> register(@RequestBody UserDto dto) {
        var user = userMapper.map(dto);
        return userService.register(user)
            .map(userMapper::map);
    }

    @PostMapping(TOKEN_PATH)
    @ResponseStatus(HttpStatus.OK)
    public Mono<AuthResponseDto> getToken(@RequestBody AuthRequestDto dto) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
        return authenticationService.authenticate(authenticationToken)
            .map(tokenService::generateToken)
            .flatMap(tokenDetails ->
                Mono.just(
                    AuthResponseDto.builder()
                        .userId(tokenDetails.getUserId())
                        .token(tokenDetails.getToken())
                        .issuedAt(tokenDetails.getIssuedAt())
                        .expiresAt(tokenDetails.getExpiresAt())
                        .build()
                ));
    }

    @PostMapping(API_KEY_PATH)
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> getApiKey(@RequestBody AuthRequestDto dto) {
        return authenticationService.getApiKey(dto.getUsername());
    }
}