package com.proselyteapi.dataprovider.security;

import annotation.Unit;
import com.proselyteapi.dataprovider.entity.Role;
import com.proselyteapi.dataprovider.entity.User;
import com.proselyteapi.dataprovider.service.TokenService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

@Unit
class JwtUtilTest {

    private TokenService tokenService = new TokenService("fbsjhweguy4t38wgdjhash43762378", 60L, "test");

    private final JwtUtil jwtUtil;

    public JwtUtilTest() {
        this.jwtUtil = new JwtUtil("fbsjhweguy4t38wgdjhash43762378");
    }
    @Test
    void getClaimsCorrectly() {
        var user = User.builder()
            .id(1L)
            .username("Vasia")
            .password("hsjahdj")
            .role(Role.USER)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var token = tokenService.generateToken(user);
        var claims = jwtUtil.getAllClaimsFromToken(token.getToken());
        var a = claims.getSubject();
    }
}