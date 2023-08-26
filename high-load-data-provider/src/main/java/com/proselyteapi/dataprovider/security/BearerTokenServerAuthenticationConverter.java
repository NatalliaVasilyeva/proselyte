package com.proselyteapi.dataprovider.security;

import com.proselyteapi.dataprovider.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RequiredArgsConstructor
public class BearerTokenServerAuthenticationConverter implements ServerAuthenticationConverter {

    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Function<String, Mono<String>> getBearerValue = authValue -> Mono.justOrEmpty(authValue.substring(BEARER_PREFIX.length()));

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange)
            .flatMap(AuthorizationHeaderPayload::extract)
            .flatMap(getBearerValue)
            .flatMap(jwtUtil::checkToken)
            .map(tokenService::parseToken);
            }
}