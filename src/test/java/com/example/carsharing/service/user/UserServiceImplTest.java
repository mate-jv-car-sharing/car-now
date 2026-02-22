package com.example.carsharing.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharing.dto.user.UserRegistrationRequestDto;
import com.example.carsharing.dto.user.UserResponseDto;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.exception.RegistrationException;
import com.example.carsharing.mapper.UserMapper;
import com.example.carsharing.model.Role;
import com.example.carsharing.model.User;
import com.example.carsharing.repository.RoleRepository;
import com.example.carsharing.repository.UserRepository;
import com.example.carsharing.util.UserTestDataFactory;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_ValidRequest_ReturnsUserResponseDto() {
        UserRegistrationRequestDto requestDto = UserTestDataFactory.getValidRegistrationRequest();
        User user = UserTestDataFactory.getUser();
        Role customerRole = UserTestDataFactory.getCustomerRole();
        User savedUser = UserTestDataFactory.getSavedUserWithRoles(customerRole);
        UserResponseDto expectedResponseDto = UserTestDataFactory.getUserResponseDto();

        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.empty());
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.RoleName.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(any(User.class))).thenReturn(expectedResponseDto);

        UserResponseDto actualResponseDto = userService.register(requestDto);

        assertNotNull(actualResponseDto);
        assertEquals(expectedResponseDto.id(), actualResponseDto.id());
        assertEquals(expectedResponseDto.email(), actualResponseDto.email());
        assertEquals(expectedResponseDto.firstName(), actualResponseDto.firstName());
        assertEquals(expectedResponseDto.lastName(), actualResponseDto.lastName());

        verify(userRepository).findByEmail(requestDto.email());
        verify(userMapper).toModel(requestDto);
        verify(passwordEncoder).encode("Password123");
        verify(roleRepository).findByName(Role.RoleName.CUSTOMER);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(any(User.class));
    }

    @Test
    void register_PasswordsDoNotMatch_ThrowsRegistrationException() {
        UserRegistrationRequestDto requestDto =
                UserTestDataFactory.getRegistrationRequestWithMismatchedPasswords();

        RegistrationException exception = assertThrows(
                RegistrationException.class,
                () -> userService.register(requestDto)
        );

        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsRegistrationException() {
        UserRegistrationRequestDto requestDto =
                UserTestDataFactory.getRegistrationRequestWithExistingEmail();
        User existingUser = UserTestDataFactory.getExistingUser();

        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(existingUser));

        RegistrationException exception = assertThrows(
                RegistrationException.class,
                () -> userService.register(requestDto)
        );

        assertEquals("Can't register user: existing@example.com email already exists",
                exception.getMessage());
        verify(userRepository).findByEmail(requestDto.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_CustomerRoleNotFound_ThrowsEntityNotFoundException() {
        UserRegistrationRequestDto requestDto = UserTestDataFactory.getValidRegistrationRequest();
        User user = UserTestDataFactory.getUser();

        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.empty());
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.RoleName.CUSTOMER)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.register(requestDto)
        );

        assertEquals("Can't find CUSTOMER role", exception.getMessage());
        verify(roleRepository).findByName(Role.RoleName.CUSTOMER);
        verify(userRepository, never()).save(any());
    }

    @Test
    void hasRole_UserHasRole_ReturnsTrue() {
        User user = UserTestDataFactory.getUserWithCustomerRoleAndId();

        boolean result = userService.hasRole(user, Role.RoleName.CUSTOMER);

        assertTrue(result);
    }

    @Test
    void hasRole_UserDoesNotHaveRole_ReturnsFalse() {
        User user = UserTestDataFactory.getUserWithCustomerRoleAndId();

        boolean result = userService.hasRole(user, Role.RoleName.MANAGER);

        assertFalse(result);
    }

    @Test
    void isManager_UserIsManager_ReturnsTrue() {
        User user = UserTestDataFactory.getUserWithManagerRoleAndId();

        boolean result = userService.isManager(user);

        assertTrue(result);
    }

    @Test
    void isManager_UserIsNotManager_ReturnsFalse() {
        User user = UserTestDataFactory.getUserWithCustomerRoleAndId();

        boolean result = userService.isManager(user);

        assertFalse(result);
    }
}
