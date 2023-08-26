package com.proselyteapi.dataprovider.config;

import com.proselyteapi.dataprovider.security.AuthenticationManager;
import com.proselyteapi.dataprovider.security.BearerTokenServerAuthenticationConverter;
import com.proselyteapi.dataprovider.security.JwtUtil;
import com.proselyteapi.dataprovider.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
public class WebSecurityConfig {

    @Value("${jwt.secret}")
    private String secret;

    private final String [] publicRoutes = {"/api/v1/register", "/api/v1/get-api-key"};

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthenticationManager authenticationManager, TokenService tokenService) {

        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges ->
                exchanges
                    .pathMatchers(HttpMethod.OPTIONS)
                    .permitAll()
                    .pathMatchers(publicRoutes)
                    .permitAll()
                    .anyExchange()
                    .authenticated()
            )
            .exceptionHandling(handling ->
                handling.authenticationEntryPoint((swe , e) -> {
                    log.error("SecurityWebFilterChain - unauthorized error: {}", e.getMessage());
                    return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
            })
                .accessDeniedHandler((swe, e) -> {
                    log.error("SecurityWebFilterChain - access denied: {}", e.getMessage());

                    return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
            }))
            .addFilterAt(bearerAuthenticationFilter(authenticationManager, tokenService), SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }

    @Bean
    public AuthenticationWebFilter bearerAuthenticationFilter(AuthenticationManager authenticationManager, TokenService tokenService) {
        AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(authenticationManager);
        bearerAuthenticationFilter.setServerAuthenticationConverter(new BearerTokenServerAuthenticationConverter(tokenService, new JwtUtil(secret)));
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));

        return bearerAuthenticationFilter;
    }

}