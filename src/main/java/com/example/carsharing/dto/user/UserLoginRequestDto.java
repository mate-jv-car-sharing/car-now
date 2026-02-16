package com.example.carsharing.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User login request")
public record UserLoginRequestDto(
        @Schema(description = "User email", example = "user@example.com")
        @NotBlank(message = "{user.request.email.notblank}")
        @Email(message = "{user.request.email.format}")
        String email,

        @Schema(
                description = "User password.8-30 chars, must contain uppercase, lowercase, digit",
                example = "Password123"
        )
        @Size(min = 8, max = 30, message = "${user.request.password.size}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "{user.request.password.format}"
        )
        String password
) {
}
