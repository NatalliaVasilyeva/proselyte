package com.proselyteapi.dataprovider.security;

import com.proselyteapi.dataprovider.controller.AuthRestController;
import com.proselyteapi.dataprovider.entity.ApiKey;
import com.proselyteapi.dataprovider.repository.ApiKeyRepository;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class ApiKeyAuthFilter implements WebFilter {

    private final ApiKeyRepository apiKeyRepository;
    private static final String API_KEY_HEADER = "x-api-key";
    private static final String USERNAME = "username";
    private final List<String> freeAccessPaths = List.of(AuthRestController.TOKEN_PATH, AuthRestController.API_KEY_PATH, AuthRestController.REGISTER_PATH);

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        if (freeAccessPaths.contains(request.getURI().getPath().substring(7))) {
            return webFilterChain.filter(serverWebExchange);
        }

        String requestApiKey = request.getHeaders().getFirst(API_KEY_HEADER);
        String username = request.getHeaders().getFirst(USERNAME);
        Mono<String> apiKey = apiKeyRepository.findByUsername(username).map(ApiKey::getApikey);

        apiKey.subscribe(key -> {
            if (StringUtils.isEmpty(requestApiKey) || StringUtils.isEmpty(key) || !key.equals(requestApiKey)) {
                serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                serverWebExchange.getResponse().writeWith(s -> Mono.just("Unauthorized"));
                throw new BadCredentialsException("Invalid API Key");
            }
        });

        return webFilterChain.filter(serverWebExchange);
    }
}