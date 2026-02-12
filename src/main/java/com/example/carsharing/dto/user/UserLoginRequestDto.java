package com.example.carsharing.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @NotBlank(message = "{user.request.email.notblank}")
        @Email(message = "{user.request.email.format}")
        String email,

        @Size(min = 8, max = 30, message = "${user.request.password.size}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "{user.request.password.format}"
        )
        String password
) {
}
