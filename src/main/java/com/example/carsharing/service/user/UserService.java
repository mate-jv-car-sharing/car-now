package com.example.carsharing.service.user;

import com.example.carsharing.dto.user.UserRegistrationRequestDto;
import com.example.carsharing.dto.user.UserResponseDto;
import com.example.carsharing.model.Role;
import com.example.carsharing.model.User;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto);

    boolean hasRole(User user, Role.RoleName roleName);

    boolean isManager(User user);
}
