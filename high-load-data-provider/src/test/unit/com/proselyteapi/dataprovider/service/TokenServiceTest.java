package com.proselyteapi.dataprovider.service;

import annotation.Unit;
import com.proselyteapi.dataprovider.domain.TokenClaimData;
import com.proselyteapi.dataprovider.entity.Role;
import com.proselyteapi.dataprovider.entity.User;
import com.proselyteapi.dataprovider.security.UserPrincipal;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Unit
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setup() {

        this.tokenService = new TokenService("aaa", 20L, "test");
    }
    @Test
    void generateTokenTest() {
        var user = User.builder()
            .id(1L)
            .username("person")
            .password("iiiqqqq")
            .role(Role.USER)
            .enabled(true)
            .build();

        var tokenDataResult = tokenService.generateToken(user);

        assertEquals(tokenDataResult.getUserId(), user.getId());
        assertThat(tokenDataResult.getToken()).isNotNull();
    }

    @Test
    void parseTokenTest() {
        var claims = new DefaultClaims();
        claims.setId("1l");
        claims.setIssuer("test");
        claims.setSubject("100");
        claims.put("username", "test_username");
        claims.put("role", "USER");
        claims.setExpiration(Date.from(LocalDateTime.now().plusHours(48).toInstant(ZoneOffset.UTC)));
        var tokenClaimData = new TokenClaimData("token", claims);

        var result = tokenService.parseToken(tokenClaimData);
        var resultPrincipal = (UserPrincipal) result.getPrincipal();

        assertEquals(resultPrincipal.getId(), Long.parseLong(claims.getSubject()));
        assertEquals(resultPrincipal.getUsername(), claims.get("username"));
        assertEquals(result.getAuthorities().toString(), Collections.singletonList(Role.USER).toString());
    }

}