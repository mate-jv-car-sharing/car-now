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

    public boolean isRentalOwner(User user, Rental rental) {
        return rental.getUser().getId().equals(user.getId());
    }

    public void validateRentalOwnership(User user, Rental rental) {
        if (!isRentalOwner(user, rental)) {
            throw new RentalException(String.format(
                    "User with id %d is not the owner of the rental with id %d",
                    user.getId(),
                    rental.getId()
            ));
        }
    }
}
