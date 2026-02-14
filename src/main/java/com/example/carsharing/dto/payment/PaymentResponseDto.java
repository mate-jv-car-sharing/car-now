package com.example.carsharing.dto.payment;

import com.example.carsharing.model.enums.PaymentStatus;
import com.example.carsharing.model.enums.PaymentType;
import java.math.BigDecimal;

public record PaymentResponseDto(
        Long id,
        PaymentStatus status,
        PaymentType type,
        BigDecimal amountToPay,
        String sessionUrl
) {}
