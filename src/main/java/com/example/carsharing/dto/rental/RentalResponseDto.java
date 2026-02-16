package com.example.carsharing.dto.rental;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Rental information")
public record RentalResponseDto(
        @Schema(description = "Rental ID", example = "1")
        Long id,

        @Schema(description = "Rental start date", example = "2026-02-16")
        LocalDate rentalDate,

        @Schema(description = "Expected return date", example = "2026-02-25")
        LocalDate returnDate,

        @Schema(description = "Actual return date (null if not returned)", example = "2026-02-24")
        LocalDate actualReturnDate,

        @Schema(description = "Car ID", example = "1")
        Long carId,

        @Schema(description = "User ID", example = "1")
        Long userId
) {}
