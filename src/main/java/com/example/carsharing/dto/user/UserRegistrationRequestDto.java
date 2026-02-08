package com.example.carsharing.dto.user;

public record UserRegistrationRequestDto(
        String email,
        String firstName,
        String lastName,
        String password,
        String repeatPassword
) {}
