package com.example.carsharing.dto.user;

import com.example.carsharing.validation.annotation.FieldMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request")
@FieldMatch(firstFieldName = "password",
        secondFieldName = "repeatPassword",
        message = "{user.request.password.mismatch}")
public record UserRegistrationRequestDto(
        @Schema(description = "User email", example = "user@example.com")
        @NotBlank(message = "{user.request.email.notblank}")
        @Email(message = "{user.request.email.format}")
        String email,

        @Schema(description = "First name", example = "John")
        @NotBlank(message = "{user.request.firstname.notblank}")
        @Size(max = 255, message = "{user.request.firstname.size}")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        @NotBlank(message = "{user.request.lastname.notblank}")
        @Size(max = 255, message = "{user.request.lastname.size}")
        String lastName,

        @Schema(
                description = "Password (8-30 chars, must contain uppercase, lowercase, and digit)",
                example = "Password123"
        )
        @NotBlank(message = "{user.request.password.notblank}")
        @Size(min = 8, max = 30, message = "{user.request.password.size}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "{user.request.password.format}"
        )
        String password,

        @Schema(description = "Repeat password", example = "Password123")
        @NotBlank(message = "{user.request.repeatpassword.notblank}")
        @Size(min = 8, max = 30, message = "{user.request.repeatpassword.size}")
        String repeatPassword
) {}
