package com.example.carsharing.util;

import com.example.carsharing.dto.user.UserRegistrationRequestDto;
import com.example.carsharing.dto.user.UserResponseDto;
import com.example.carsharing.model.Role;
import com.example.carsharing.model.User;
import java.util.Set;
import org.springframework.test.util.ReflectionTestUtils;

public class UserTestDataFactory {

    public static UserRegistrationRequestDto getValidRegistrationRequest() {
        return new UserRegistrationRequestDto(
                "user@example.com",
                "John",
                "Doe",
                "Password123",
                "Password123"
        );
    }

    public static UserRegistrationRequestDto getRegistrationRequestWithMismatchedPasswords() {
        return new UserRegistrationRequestDto(
                "user@example.com",
                "John",
                "Doe",
                "Password123",
                "DifferentPassword"
        );
    }

    public static UserRegistrationRequestDto getRegistrationRequestWithExistingEmail() {
        return new UserRegistrationRequestDto(
                "existing@example.com",
                "John",
                "Doe",
                "Password123",
                "Password123"
        );
    }

    public static User getUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("Password123");
        return user;
    }

    public static User getSavedUser() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("user@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("encodedPassword");
        return user;
    }

    public static User getSavedUserWithRoles(Role... roles) {
        User user = getSavedUser();
        user.setRoles(Set.of(roles));
        return user;
    }

    public static User getExistingUser() {
        User user = new User();
        user.setEmail("existing@example.com");
        return user;
    }

    public static UserResponseDto getUserResponseDto() {
        return new UserResponseDto(
                1L,
                "user@example.com",
                "John",
                "Doe"
        );
    }

    public static Role getCustomerRole() {
        Role role = new Role();
        role.setName(Role.RoleName.CUSTOMER);
        return role;
    }

    public static Role getManagerRole() {
        Role role = new Role();
        role.setName(Role.RoleName.MANAGER);
        return role;
    }

    public static User getUserWithCustomerRoleAndId() {
        return getSavedUserWithRoles(getCustomerRole());
    }

    public static User getUserWithManagerRoleAndId() {
        return getSavedUserWithRoles(getManagerRole());
    }

    public static User getCustomerUserWithoutId(Role savedCustomerRole) {
        User user = new User();
        user.setEmail("customer@example.com");
        user.setFirstName("Customer");
        user.setLastName("Customer");
        user.setPassword("Password123");
        user.setRoles(Set.of(savedCustomerRole));
        return user;
    }

    public static User getManagerUserWithoutId(Role savedManagerRole) {
        User user = new User();
        user.setEmail("manager@example.com");
        user.setFirstName("Manager");
        user.setLastName("Manager");
        user.setPassword("Password123");
        user.setRoles(Set.of(savedManagerRole));
        return user;
    }
}
