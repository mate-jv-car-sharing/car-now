package com.example.carsharing.service.user;

import com.example.carsharing.dto.user.UserRegistrationRequestDto;
import com.example.carsharing.dto.user.UserResponseDto;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.exception.RegistrationException;
import com.example.carsharing.mapper.UserMapper;
import com.example.carsharing.model.Role;
import com.example.carsharing.model.User;
import com.example.carsharing.repository.RoleRepository;
import com.example.carsharing.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (!requestDto.password().equals(requestDto.repeatPassword())) {
            throw new RegistrationException("Passwords do not match");
        }

        if (userRepository.findByEmail((requestDto.email())).isPresent()) {
            throw new RegistrationException("Can't register user: " + requestDto.email()
                    + " email already exists");
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role defaultRole = roleRepository.findByName(Role.RoleName.CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException("Can't find "
                        + Role.RoleName.CUSTOMER + " role"));
        user.setRoles(Set.of(defaultRole));
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public boolean hasRole(User user, Role.RoleName roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    @Override
    public boolean isManager(User user) {
        return hasRole(user, Role.RoleName.MANAGER);
    }
}
