package com.proselyteapi.dataprovider.controller;

import com.proselyteapi.dataprovider.dto.AuthRequestDto;
import com.proselyteapi.dataprovider.dto.AuthResponseDto;
import com.proselyteapi.dataprovider.dto.UserDto;
import com.proselyteapi.dataprovider.mapper.UserMapper;
import com.proselyteapi.dataprovider.service.TokenService;
import com.proselyteapi.dataprovider.service.AuthenticationService;
import com.proselyteapi.dataprovider.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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


    @PostMapping("/register")
    public Mono<UserDto> register(@RequestBody UserDto dto) {
        var user = userMapper.map(dto);
        return userService.register(user)
            .map(userMapper::map);
    }

    @PostMapping("/get-api-key")
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
}