package com.proselyteapi.dataprovider.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.proselyteapi.dataprovider.dto.UserDto;
import com.proselyteapi.dataprovider.entity.User;
import com.proselyteapi.dataprovider.mapper.UserMapper;
import com.proselyteapi.dataprovider.repository.UserRepository;
import com.proselyteapi.dataprovider.service.AuthenticationService;
import com.proselyteapi.dataprovider.service.TokenService;
import com.proselyteapi.dataprovider.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class AuthRestControllerIT extends ApiBaseTest {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    private final UserMapper userMapper = UserMapper.MAPPER;

    private String existingUserId;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @AfterEach
    public void after(){
        userRepository.deleteAll().subscribe();
        super.after();
    }

    @Test
    void fakeTest() {
        assertTrue(true);
    }

    @Test
    void contextLoads(ApplicationContext context) {
        assertThat(context).isNotNull();
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
            .hasSize(1);


//        webTestClient.post().uri("/api/v1/register")
//            .bodyValue(jsonBody)
//            .header(commonHeaders.toString())
//            .exchange()
//            .expectStatus().isCreated()
////            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
//            .expectBodyList(UserDto.class)
//            .hasSize(1)
//            .consumeWith(response -> {
//                List<UserDto> users = response.getResponseBody();
//                users.forEach( u ->{
//                    assertTrue(u.getId() != null);
//                    assertTrue(u.getUpdatedAt() != null);
//                    assertTrue(u.getCreatedAt() != null);
//                    assertEquals(u.getUsername(), userDto.getUsername());
//                });
//            });
    }
}