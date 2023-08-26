package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.domain.SecurityUserDetails;
import com.proselyteapi.dataprovider.entity.User;
import com.proselyteapi.dataprovider.entity.Role;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public Mono<User> register(User user) {
        return userRepository.save(
            user.toBuilder()
                .password(passwordEncoder.encode(user.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        ).doOnSuccess(u ->
            log.info("User service - user: {} was created", u)
        );
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
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with such username not found!")))
            .cast(UserDetails.class);
    }
}