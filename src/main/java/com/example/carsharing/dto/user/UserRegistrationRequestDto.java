package com.example.carsharing.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequestDto(

    String email,

    String firstName,

    String lastName,

    String password,

    String repeatPassword
) {}