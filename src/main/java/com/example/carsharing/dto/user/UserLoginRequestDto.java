package com.example.carsharing.dto.user;

public record UserLoginRequestDto(
        String email,
        String password
) {
}
