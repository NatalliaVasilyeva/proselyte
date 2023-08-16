package com.proselyteapi.dataprovider.dto;
import lombok.Data;

@Data
public class AuthRequestDto {
    private String username;
    private String password;
}