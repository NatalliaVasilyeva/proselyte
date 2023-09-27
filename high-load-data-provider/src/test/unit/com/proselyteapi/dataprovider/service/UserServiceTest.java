package com.proselyteapi.dataprovider.service;

import annotation.Unit;
import com.proselyteapi.dataprovider.domain.SecurityUserDetails;
import com.proselyteapi.dataprovider.entity.ApiKey;
import com.proselyteapi.dataprovider.entity.Role;
import com.proselyteapi.dataprovider.entity.User;
import com.proselyteapi.dataprovider.repository.ApiKeyRepository;
import com.proselyteapi.dataprovider.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ApiKeyRepository apiKeyRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setup() {
        this.userService = new UserService(userRepository, apiKeyRepository, passwordEncoder);
    }

    @Test
    void registerUserTest() {
        var user = User.builder()
            .username("person")
            .password("iiiqqqq")
            .role(Role.USER)
            .enabled(true)
            .build();

        var savedUser = User.builder()
            .username("person")
            .password("iiiqqqq")
            .role(Role.USER)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var savedApiKey = ApiKey.builder()
            .id(1l)
            .username("person")
            .apikey(UUID.randomUUID().toString())
            .createdAt(LocalDateTime.now())
            .build();

        when(passwordEncoder.encode(anyString())).thenReturn("iiiqqqq");
        when(userRepository.save(user)).thenReturn(Mono.just(savedUser));
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(Mono.just(savedApiKey));

        var registeredUser = userService.register(user);
        StepVerifier.create(registeredUser)
            .expectNext(savedUser)
            .expectComplete()
            .verify();

        verify(userRepository, times(1)).save(user);
        verify(passwordEncoder, times(1)).encode("iiiqqqq");
        verify(apiKeyRepository, times(1)).save(any(ApiKey.class));
    }

    @Test
    void getUserByIdTest() {
        var userId = 100L;

        var savedUser = User.builder()
            .id(100L)
            .username("person")
            .password("iiiqqqq")
            .role(Role.USER)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(userRepository.findById(userId)).thenReturn(Mono.just(savedUser));

        var user = userService.getUserById(userId);

        StepVerifier.create(user)
            .expectNext(savedUser)
            .expectComplete()
            .verify();

        verify(userRepository, times(1)).findById(userId);

    }

    @Test
    void findByUsernameTest() {
        var username = "person";

        var user = User.builder()
            .id(100L)
            .username("person")
            .password("iiiqqqq")
            .role(Role.USER)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var userDetails =
            new SecurityUserDetails(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(user.getRole()),
                user.getId());

        when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));

        var userDetailsResult = userService.findByUsername(username);

        StepVerifier.create(userDetailsResult)
            .expectNext(userDetails)
            .expectComplete()
            .verify();

        verify(userRepository, times(1)).findByUsername(username);

    }

    @Test
    void findByUsernameThrowExceptionIfUserNotExistsTest() {
        var username = "person";
        when(userRepository.findByUsername(username)).thenReturn(Mono.empty());

        var result = userService.findByUsername(username);

        StepVerifier.create(result)
            .expectError(UsernameNotFoundException.class)
            .verify();
        verify(userRepository, times(1)).findByUsername(username);

    }

}