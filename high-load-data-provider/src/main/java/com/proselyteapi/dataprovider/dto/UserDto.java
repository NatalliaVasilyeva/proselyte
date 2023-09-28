package com.proselyteapi.dataprovider.dto;

import com.proselyteapi.dataprovider.entity.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private Role role = Role.USER;
    private boolean enabled = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}