package com.proselyteapi.dataprovider.security;

import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class AuthorizationHeaderPayload {

  private AuthorizationHeaderPayload() {
    //sonar requirements
  }

  public static Mono<String> extract(ServerWebExchange serverWebExchange) {
    return Mono.justOrEmpty(serverWebExchange.getRequest()
        .getHeaders()
        .getFirst(HttpHeaders.AUTHORIZATION));
  }
}