package com.proselyteapi.dataprovider.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.proselyteapi.dataprovider.dto.AuthRequestDto;
import com.proselyteapi.dataprovider.dto.AuthResponseDto;
import com.proselyteapi.dataprovider.dto.UserDto;
import com.proselyteapi.dataprovider.entity.Role;
import com.proselyteapi.dataprovider.entity.User;
import com.proselyteapi.dataprovider.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthRestControllerIT extends ApiBaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    public void teardown() {
        userRepository.deleteAll().subscribe();
    }

    @Test
    void registerUserIT() throws JsonProcessingException {

        var userDto = new UserDto();
        userDto.setUsername("Vasia");
        userDto.setPassword("12345678");

        final String jsonBody = objectMapper.writeValueAsString(userDto);
        webTestClient.post().uri("/api/v1/register")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(jsonBody)
            .exchange()
            .expectStatus().isCreated()
            .expectBodyList(UserDto.class)
            .hasSize(1)
            .consumeWith(response -> {
                List<UserDto> users = response.getResponseBody();
                users.forEach(u -> {
                    assertNotNull(u.getId());
                    assertNotNull(u.getUpdatedAt());
                    assertNotNull(u.getCreatedAt());
                    assertEquals(u.getUsername(), userDto.getUsername());
                });
            });
    }

    @Test
    void getTokenIT() throws JsonProcessingException {
        var password = "12345678";

        var user = new User().toBuilder()
            .username("Vasia")
            .password(passwordEncoder.encode(password))
            .role(Role.USER)
            .enabled(true)
            .build();

        var savedUser = userRepository.save(user).block();

        var authRequestDto = new AuthRequestDto();
        authRequestDto.setUsername(user.getUsername());
        authRequestDto.setPassword(password);
        final String jsonBody = objectMapper.writeValueAsString(authRequestDto);


        webTestClient.post().uri("/api/v1/get-token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(jsonBody)
            .exchange()
            .expectStatus().isOk()
            .expectBody(AuthResponseDto.class)
            .consumeWith(response -> {
               var body = response.getResponseBody();
                    assertEquals(savedUser.getId(), body.getUserId());
                    assertNotNull(body.getToken());
                    assertNotNull(body.getIssuedAt());
                    assertNotNull(body.getExpiresAt());
            });
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER", "ADMIN"})
    void getApiKeyIT() throws JsonProcessingException {
        var password = "12345678";

        var user = new User().toBuilder()
            .username("Vasia")
            .password(passwordEncoder.encode(password))
            .role(Role.USER)
            .enabled(true)
            .build();

        var authRequestDto = new AuthRequestDto();
        authRequestDto.setUsername(user.getUsername());
        authRequestDto.setPassword(password);
        final String jsonBody = objectMapper.writeValueAsString(authRequestDto);


        webTestClient.post().uri("/api/v1/get-api-key")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(jsonBody)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class);
    }
}