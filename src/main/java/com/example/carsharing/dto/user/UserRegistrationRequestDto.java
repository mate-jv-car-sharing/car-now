package com.example.carsharing.dto.user;

import com.example.carsharing.validation.annotation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@FieldMatch(firstFieldName = "password",
        secondFieldName = "repeatPassword",
        message = "{user.request.password.mismatch}")
public record UserRegistrationRequestDto(
        @NotBlank(message = "{user.request.email.notblank}")
        @Email(message = "{user.request.email.format}")
        String email,

        @NotBlank(message = "{user.request.firstname.notblank}")
        @Size(max = 255, message = "{user.request.firstname.size}")
        String firstName,

        @NotBlank(message = "{user.request.lastname.notblank}")
        @Size(max = 255, message = "{user.request.lastname.size}")
        String lastName,

        @NotBlank(message = "{user.request.password.notblank}")
        @Size(min = 8, max = 30, message = "{user.request.password.size}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "{user.request.password.format}"
        )
        String password,

        @NotBlank(message = "{user.request.repeatpassword.notblank}")
        @Size(min = 8, max = 30, message = "{user.request.repeatpassword.size}")
        String repeatPassword
) {}
