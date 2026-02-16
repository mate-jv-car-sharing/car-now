package com.example.carsharing.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User information")
public record UserResponseDto(
        @Schema(description = "User ID", example = "1")
        Long id,

        @Schema(description = "User email", example = "user@example.com")
        String email,

        @Schema(description = "First name", example = "John")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        String lastName
) {}
