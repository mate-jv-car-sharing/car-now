package com.example.carsharing.dto.car;

import com.example.carsharing.model.enums.CarType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Car information")
public record CarResponseDto(
        @Schema(description = "Car ID", example = "1")
        Long id,

        @Schema(description = "Car model", example = "Model 3")
        String model,

        @Schema(description = "Car brand", example = "Tesla")
        String brand,

        @Schema(description = "Car type", example = "SEDAN")
        CarType type,

        @Schema(description = "Available inventory", example = "5")
        int inventory,

        @Schema(description = "Daily rental fee in USD", example = "50.00")
        BigDecimal dailyFee
) {}
