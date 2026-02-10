package com.example.carsharing.dto.rental;

import java.time.LocalDate;

public record CreateRentalRequestDto(
        LocalDate returnDate,
        Long carId
) {}
