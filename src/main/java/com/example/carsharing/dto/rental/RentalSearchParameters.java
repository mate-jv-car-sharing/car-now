package com.example.carsharing.dto.rental;

public record RentalSearchParameters(
        String[] userId,
        String[] isActive
) {
}
