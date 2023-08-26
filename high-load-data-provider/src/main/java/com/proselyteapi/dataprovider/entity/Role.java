package com.proselyteapi.dataprovider.entity;

import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Optional;

public enum Role implements GrantedAuthority {
    USER,
    ADMIN;

    public static Optional<Role> find(String role) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(role))
            .findFirst();
    }
    @Override
    public String getAuthority() {
        return name();
    }
}