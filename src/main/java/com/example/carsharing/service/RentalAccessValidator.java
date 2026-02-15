package com.example.carsharing.service;

import com.example.carsharing.exception.RentalException;
import com.example.carsharing.model.Rental;
import com.example.carsharing.model.User;
import com.example.carsharing.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RentalAccessValidator {
    private final UserService userService;

    public void validateRentalOwnership(User user, Rental rental) {
        if (!rental.getUser().getId().equals(user.getId())) {
            throw new RentalException(String.format(
                    "User with id %d is not the owner of the rental with id %d",
                    user.getId(),
                    rental.getId()
            ));
        }
    }
}
