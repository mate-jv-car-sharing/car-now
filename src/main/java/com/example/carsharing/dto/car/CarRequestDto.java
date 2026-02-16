package com.example.carsharing.dto.car;

import com.example.carsharing.model.enums.CarType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request to create or update a car")
public record CarRequestDto(
        @Schema(description = "Car model", example = "Model 3")
        @NotBlank(message = "{car.request.model.notblank}")
        String model,

        @Schema(description = "Car brand", example = "Tesla")
        @NotBlank(message = "{car.request.brand.notblank}")
        String brand,

        @Schema(description = "Car type", example = "SEDAN")
        @NotNull(message = "{car.request.type.notnull}")
        CarType type,

        @Schema(description = "Available inventory", example = "5")
        @Min(value = 0, message = "{car.request.inventory.min}")
        int inventory,

        @Schema(description = "Daily rental fee in USD", example = "50.00")
        @NotNull(message = "Daily fee must not be null")
        @DecimalMin(value = "1.0", inclusive = true, message = "{car.request.dailyfee.min}")
        @Digits(integer = 3, fraction = 2, message = "{car.request.dailyfee.max}")
        BigDecimal dailyFee
) {
}
