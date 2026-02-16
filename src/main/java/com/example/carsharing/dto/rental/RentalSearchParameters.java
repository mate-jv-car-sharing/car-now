package com.example.carsharing.dto.rental;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Search parameters for filtering rentals")
public record RentalSearchParameters(
        @Schema(description = "Filter by user ID (for managers)", example = "1")
        String[] userId,

        @Schema(description = "Filter by active status", example = "true")
        String[] isActive
) {
}
