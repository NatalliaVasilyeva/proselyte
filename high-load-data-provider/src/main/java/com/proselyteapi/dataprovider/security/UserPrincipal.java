package com.proselyteapi.dataprovider.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.security.auth.Subject;
import java.security.Principal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Principal {

    private Long id;
    private String username;

    @Override
    public String getName() {
        return username;
    }
    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}