package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.domain.SecurityUserDetails;
import com.proselyteapi.dataprovider.entity.User;
import com.proselyteapi.dataprovider.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final ApiKeyUtils apiKeyUtils;

    private final Scheduler scheduler = Schedulers.newParallel("password-encoder", Schedulers.DEFAULT_POOL_SIZE, true);

    public Mono<User> authenticate(final Authentication authentication) {
        return getAuthenticatedUser(authentication)
            .cast(SecurityUserDetails.class)
            .map(result -> new User(
                result.getId(),
                result.getUsername(),
                result.getPassword(),
                (Role) result.getAuthorities().stream().findFirst().get(),
                result.isEnabled(),
                null,
                null));
    }

    public Mono<String> getApiKey(String username, String password) {
       return userService.isUserRegister(username,password)
            .flatMap(isRegister -> {
                if (isRegister) {
                    return Mono.just(apiKeyUtils.getApiKey());
                } else {
                    return Mono.empty();
                }
            });
    }

    private Mono<UserDetails> getAuthenticatedUser(Authentication authentication) {
        var username = authentication.getName();
        var user = retrieveUser(username).doOnNext(preAuthenticationChecks::check);
        return authentication.isAuthenticated()
            ? user
            : checkPasswordIfUserNotAuthenticated(user, (String) authentication.getCredentials());
    }

    private Mono<UserDetails> checkPasswordIfUserNotAuthenticated(Mono<UserDetails> user, String password) {
        return user
            .publishOn(scheduler)
            .filter(u -> passwordEncoder.matches(password, u.getPassword()))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new BadCredentialsException("Invalid credentials"))))
            .doOnNext(postAuthenticationChecks::check);
    }

    private Mono<UserDetails> retrieveUser(final String username) {
        return userService.findByUsername(username)
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username")));
    }

    private final UserDetailsChecker preAuthenticationChecks = user -> {
        if (!user.isEnabled()) {
            throw new DisabledException("User is disabled");
        }
        if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("User account has expired");
        }
    };

    private final UserDetailsChecker postAuthenticationChecks = user -> {
        if (!user.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("User credentials have expired");
        }
    };
}