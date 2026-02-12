package com.example.carsharing.dto.car;

import com.example.carsharing.model.enums.CarType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CarRequestDto(
        @NotBlank(message = "{car.request.model.notblank}")
        String model,

        @NotBlank(message = "{car.request.brand.notblank}")
        String brand,

        @NotNull(message = "{car.request.type.notnull}")
        CarType type,

        @Min(value = 0, message = "{car.request.inventory.min}")
        int inventory,

        @NotNull(message = "Daily fee must not be null")
        @DecimalMin(value = "1.0", inclusive = true, message = "{car.request.dailyfee.min}")
        @Digits(integer = 3, fraction = 2, message = "{car.request.dailyfee.max}")
        BigDecimal dailyFee
) {
}
