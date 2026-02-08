package com.example.carsharing.dto.car;

import com.example.carsharing.model.CarType;
import java.math.BigDecimal;

public record CarResponseDto(
        Long id,
        String model,
        String brand,
        CarType type,
        int inventory,
        BigDecimal dailyFee
) {}
