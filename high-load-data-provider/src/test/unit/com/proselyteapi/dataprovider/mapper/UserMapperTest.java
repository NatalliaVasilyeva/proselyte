package com.proselyteapi.dataprovider.mapper;

import annotation.Unit;
import com.proselyteapi.dataprovider.dto.UserDto;
import com.proselyteapi.dataprovider.entity.Role;
import com.proselyteapi.dataprovider.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Unit
class UserMapperTest {

    @Test
    void shouldReturnCorrectEntity() {
        var userDto = new UserDto();
        userDto.setUsername("Vasia");
        userDto.setPassword("12345678");

        var user = UserMapper.MAPPER.map(userDto);

        assertEquals(userDto.getUsername(), user.getUsername());
        assertThat(user.getPassword()).isNotNull();
        assertEquals(Role.USER, user.getRole());
        assertFalse(user.isEnabled());
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getCreatedAt()).isNull();
    }

    @Test
    void shouldReturnCorrectDto() {
        var user = User.builder()
            .id(1L)
            .username("Vasia")
            .password("hsjahdj")
            .role(Role.USER)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var userDto = UserMapper.MAPPER.map(user);

        assertEquals(user.getUsername(), userDto.getUsername());
        assertEquals(user.getPassword(), userDto.getPassword());
        assertEquals(user.getRole(), userDto.getRole());
        assertEquals(user.isEnabled(), userDto.isEnabled());
        assertEquals(user.getCreatedAt(), userDto.getCreatedAt());
        assertEquals(user.getUpdatedAt(), userDto.getUpdatedAt());

    }
}