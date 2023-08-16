package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.domain.TokenClaimData;
import com.proselyteapi.dataprovider.entity.User;
import com.proselyteapi.dataprovider.security.TokenData;
import com.proselyteapi.dataprovider.security.UserPrincipal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expirationTimeInMinutes;

    @Value("${jwt.issuer}")
    private String issuer;
    private static final String ROLE = "role";
    private static final String USERNAME = "username";

    public TokenData generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLE, user.getRole());
        claims.put(USERNAME, user.getUsername());
        return generateToken(claims, user.getId().toString());
    }

    public UsernamePasswordAuthenticationToken parseToken(TokenClaimData data) {
        var claims = data.claims();
        var principalId = Long.parseLong(claims.getSubject());
        var role = claims.get(ROLE, String.class);
        var username = claims.get(USERNAME, String.class);

        var authorities = List.of(new SimpleGrantedAuthority(role));
        var principal = new UserPrincipal(principalId, username);
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);

    }

    private TokenData generateToken(Map<String, Object> claims, String subject) {
        var expirationTimeInMillis = expirationTimeInMinutes * 60 * 1000L;
        var expirationDate = Date.from(Instant.now().plusMillis(expirationTimeInMillis));
        return generateToken(claims, subject, expirationDate);
    }

    private TokenData generateToken(Map<String, Object> claims, String subject, Date expirationDate) {
        var issuedDate = new Date();
        String token = Jwts.builder()
            .setClaims(claims)
            .setIssuer(issuer)
            .setSubject(subject)
            .setIssuedAt(issuedDate)
            .setExpiration(expirationDate)
            .setId(UUID.randomUUID().toString())
            .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.getBytes()))
            .compact();

        return TokenData.builder()
            .userId(Long.valueOf(subject))
            .token(token)
            .issuedAt(issuedDate)
            .expiresAt(expirationDate)
            .build();
    }

}