package com.proselyteapi.dataprovider.security;

import com.proselyteapi.dataprovider.domain.TokenClaimData;
import com.proselyteapi.dataprovider.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;


import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.CredentialsExpiredException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public JwtUtil(String secret) {
        this.secret = secret;
    }

    public Mono<TokenClaimData> checkToken(String token) {
        return Mono.just(verifyToken(token))
            .onErrorResume(ex -> Mono.error(new UnauthorizedException(ex.getMessage())));
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(decodeBase64ToString(secret))
            .parseClaimsJws(token)
            .getBody();
    }


    private TokenClaimData verifyToken(String token) {
        var claims = getAllClaimsFromToken(token);
        if (isTokenExpired(claims)) {
            throw new CredentialsExpiredException("Token expired");
        }
        return new TokenClaimData(token, claims);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpirationDateFromToken(String token) {

        return getClaimFromToken(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private boolean isTokenExpired(Claims claims) {
        final Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    private String decodeBase64ToString(String s) {
        byte[] b = decodeBase64(s);
        if (b.length != 0) {
            return new String(b, StandardCharsets.UTF_8);
        }
        return null;
    }

    private byte[] decodeBase64(String secret) {
        if (StringUtils.isNotEmpty(secret)) {
            return Base64.getEncoder().encode(secret.getBytes(StandardCharsets.UTF_8));
        }
        return new byte[]{};
    }

}