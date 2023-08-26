package com.proselyteapi.dataprovider.exception;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice(basePackages = "com.proselyteapi.dataprovider")
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({UnauthorizedException.class})
    public Mono<ResponseEntity<RestErrorResponse>> handleUnauthorizedException(UnauthorizedException ex) {
        log.error("Unauthorized exception name: {}, message: ({}):", ex.getClass().getName(), ex.getMessage(), ex);
        return createResponse(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({JwtException.class})
    public Mono<ResponseEntity<RestErrorResponse>> handleJwtException(JwtException ex) {
        log.error("Jwt exception name: {}, message: ({}):", ex.getClass().getName(), ex.getMessage(), ex);
        return createResponse(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({AuthenticationException.class})
    public Mono<ResponseEntity<RestErrorResponse>> handleAccountStatusException(AccountStatusException ex) {
        log.error("Authentication exception name: {}, message: ({}):", ex.getClass().getName(), ex.getMessage(), ex);
        return createResponse(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public Mono<ResponseEntity<RestErrorResponse>> handleAuthenticationException() {
        return createResponse("Forbidden", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<RestErrorResponse>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return createResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({RuntimeException.class, Exception.class})
    public Mono<ResponseEntity<RestErrorResponse>> handleException(Exception ex) {
        log.error("(Runtime-)Exception raised: {}, stacktrace: {}", ex.getMessage(), ex.getStackTrace(), ex);
        return createResponse("Internal server error occurred. Please, contact administrator", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Mono<ResponseEntity<RestErrorResponse>> createResponse(Exception ex, HttpStatus status) {
        return createResponse(Collections.singletonList(ex.getMessage()), status);
    }

    private Mono<ResponseEntity<RestErrorResponse>> createResponse(String message, HttpStatus status) {
        return createResponse(Collections.singletonList(message), status);
    }

    private Mono<ResponseEntity<RestErrorResponse>> createResponse(List<String> messages, HttpStatus status) {
        return Mono.just(new ResponseEntity<>(RestErrorResponse.builder()
            .messages(messages)
            .status(status)
            .time(LocalDateTime.now())
            .build(), status));
    }
}