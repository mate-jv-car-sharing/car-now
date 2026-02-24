package com.example.carsharing.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharing.dto.user.UserLoginRequestDto;
import com.example.carsharing.dto.user.UserLoginResponseDto;
import com.example.carsharing.dto.user.UserRegistrationRequestDto;
import com.example.carsharing.dto.user.UserResponseDto;
import com.example.carsharing.exception.CustomGlobalExceptionHandler;
import com.example.carsharing.exception.RegistrationException;
import com.example.carsharing.security.AuthenticationService;
import com.example.carsharing.security.JwtUtil;
import com.example.carsharing.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = AuthenticationController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@Import(CustomGlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {
    private static final String AUTH_REGISTRATION_URL = "/auth/registration";
    private static final String AUTH_LOGIN_URL = "/auth/login";


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    JwtUtil jwtUtil;

    @MockitoBean
    UserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Saves new user and returns 200 with user dto when request is valid")
    void register_WhenValidRequest_ThenReturnsUserAnd200() throws Exception {
        UserRegistrationRequestDto request = getValidUserRegistrationRequestDto();
        UserResponseDto expectedResponse = getUserResponseDtoFromRequest(request);

        when(userService.register(request)).thenReturn(expectedResponse);

        mockMvc.perform(post(AUTH_REGISTRATION_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.firstName").value(request.firstName()))
                .andExpect(jsonPath("$.lastName").value(request.lastName()));
    }

    @Test
    @DisplayName("Saves new user and returns 200 with user dto when request is valid")
    void register_WhenInValidEmail_ThenReturns400() throws Exception {
        UserRegistrationRequestDto request = getValidUserRegistrationRequestDto();
        UserRegistrationRequestDto invalidEmailRequest = new UserRegistrationRequestDto(
                "invalid-email",
                request.firstName(),
                request.lastName(),
                request.password(),
                request.repeatPassword()
        );

        mockMvc.perform(post(AUTH_REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value(containsString("email")));
    }

    @Test
    @DisplayName("Returns 400 with error message when first name is blank")
    void register_WhenBlankFirstName_ThenReturns400() throws Exception {
        UserRegistrationRequestDto request = getValidUserRegistrationRequestDto();
        UserRegistrationRequestDto emptyFirstNameRequest = new UserRegistrationRequestDto(
                request.email(),
                "",
                request.lastName(),
                request.password(),
                request.repeatPassword()
        );

        mockMvc.perform(post(AUTH_REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyFirstNameRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value(containsString("firstName")));
    }

    @Test
    @DisplayName("Returns 400 with error message when password and repeat password do not match")
    void register_WhenPasswordMismatchByFieldMatch_ThenReturns400() throws Exception {
        UserRegistrationRequestDto request = getValidUserRegistrationRequestDto();
        UserRegistrationRequestDto passwordMismatchRequest = new UserRegistrationRequestDto(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password(),
                "DifferentPassword123!"
        );

        mockMvc.perform(post(AUTH_REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordMismatchRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value(containsString("repeat password")));

    }

    @Test
    @DisplayName("Returns 409 with error message when email already exists and service throws RegistrationException")
    void register_WhenEmailAlreadyExistsAndServiceThrowsRegistrationException_ThenReturns409() throws Exception {
        UserRegistrationRequestDto request = getValidUserRegistrationRequestDto();
        String message = "Can't register user: " + request.email() + " email already exists";

        when(userService.register(request)).thenThrow(new RegistrationException(message));

        mockMvc.perform(post(AUTH_REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").value(message));
    }

    @Test
    @DisplayName("Returns 200 with JWT token when login request is valid")
    void login_WhenValidRequest_ThenReturnsTokenAnd200() throws Exception {
        UserLoginRequestDto request = getValidUserLoginRequestDto();
        UserLoginResponseDto expected = new UserLoginResponseDto("jwt-token");

        when(authenticationService.authenticate(request)).thenReturn(expected);

        mockMvc.perform(post(AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(expected.token()));
    }

    @Test
    @DisplayName("Returns 400 with error message when email is blank")
    void login_WhenBlankEmail_ThenReturns400() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "",
                "Password123"
        );

        mockMvc.perform(post(AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value(containsString("email")));
    }

    @Test
    @DisplayName("Returns 400 with error message when email format is invalid")
    void login_WhenInvalidEmailFormat_ThenReturns400() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "bad-email",
                "Password123"
        );

        mockMvc.perform(post(AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value(containsString("email")));
    }

    @Test
    @DisplayName("Returns 400 with error message when password pattern is invalid")
    void login_WhenPasswordPatternInvalid_ThenReturns400() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "user@example.com",
                "passwordpassword"
        );

        mockMvc.perform(post(AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value(containsString("password")));
    }

    private UserRegistrationRequestDto getValidUserRegistrationRequestDto() {
        return new UserRegistrationRequestDto(
                "test.user@example.com",
                "User",
                "Valid",
                "Strongpass123!",
                "Strongpass123!"
        );
    }

    private UserResponseDto getUserResponseDtoFromRequest(UserRegistrationRequestDto requestDto) {
        return new UserResponseDto(
                1L,
                requestDto.email(),
                requestDto.firstName(),
                requestDto.lastName()
        );
    }

    private UserLoginRequestDto getValidUserLoginRequestDto() {
        return new UserLoginRequestDto(
                "user@example.com",
                "Password123!"
        );
    }
}
