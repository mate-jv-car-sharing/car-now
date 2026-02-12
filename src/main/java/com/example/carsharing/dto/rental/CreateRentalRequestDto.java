package com.example.carsharing.dto.rental;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateRentalRequestDto(
        @NotNull(message = "{rental.request.returnDate.notnull}")
        @Future(message = "{rental.request.returnDate.future}")
        LocalDate returnDate,

        @NotNull(message = "{rental.request.carId.notnull}")
        @Positive(message = "{rental.request.carId.positive}")
        Long carId
) {}
