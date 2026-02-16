package com.example.carsharing.dto.rental;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Request to create a rental")
public record CreateRentalRequestDto(
        @Schema(description = "Expected return date", example = "2026-02-25")
        @NotNull(message = "{rental.request.returnDate.notnull}")
        @Future(message = "{rental.request.returnDate.future}")
        LocalDate returnDate,

        @Schema(description = "Car ID", example = "1")
        @NotNull(message = "{rental.request.carId.notnull}")
        @Positive(message = "{rental.request.carId.positive}")
        Long carId
) {}
