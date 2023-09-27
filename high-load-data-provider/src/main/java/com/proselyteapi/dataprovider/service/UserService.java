package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.domain.SecurityUserDetails;
import com.proselyteapi.dataprovider.entity.ApiKey;
import com.proselyteapi.dataprovider.entity.User;
import com.proselyteapi.dataprovider.entity.Role;
import com.proselyteapi.dataprovider.repository.ApiKeyRepository;
import com.proselyteapi.dataprovider.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final ApiKeyRepository apiKeyRepository;

    private final PasswordEncoder passwordEncoder;

    public Mono<User> register(User user) {
        return userRepository.save(
                user.toBuilder()
                    .password(passwordEncoder.encode(user.getPassword()))
                    .role(user.getRole() == null ? Role.USER : user.getRole())
                    .enabled(user.isEnabled())
//                    .createdAt(user.getCreatedAt() == null ? LocalDateTime.now() : user.getCreatedAt())
//                    .updatedAt(user.getUpdatedAt() == null ? LocalDateTime.now() : user.getUpdatedAt())
                    .build()
            ).doOnSuccess(u ->
                log.info("User service - user: {} was created", u)
            ).doOnNext(savedUser ->
                apiKeyRepository.save(
                        ApiKey.builder()
                            .username(savedUser.getUsername())
                            .apikey(UUID.randomUUID().toString())
                            .createdAt(LocalDateTime.now())
                            .build()
                    )
                    .subscribe()
            )
            .doOnSuccess(key ->
                log.info("Key for user {} was created", key.getUsername()));
    }

    public Mono<Boolean> isUserRegister(String username, String password) {
        return findByUsername(username)
            .filter(Objects::nonNull)
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with such username not found!")))
            .map(u -> passwordEncoder.matches(password, u.getPassword()));
    }

    public Mono<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        return userRepository.findByUsername(username)
            .filter(Objects::nonNull)
            .map(user -> new SecurityUserDetails(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(user.getRole()),
                user.getId()))
            .switchIfEmpty(Mono.error(() -> new UsernameNotFoundException("User with such username not found!")))
            .cast(UserDetails.class);
    }
}