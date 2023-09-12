package com.proselyteapi.dataprovider.security;

import annotation.Unit;
import com.proselyteapi.dataprovider.entity.Role;
import com.proselyteapi.dataprovider.entity.User;
import com.proselyteapi.dataprovider.security.JwtUtil;
import com.proselyteapi.dataprovider.service.TokenService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
        System.out.println(a);
//        assertThat(jwt.getSubject(), is(notNullValue()));
//        assertThat(jwt.getSubject(), is("1234567890"));
    }
}