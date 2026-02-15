package com.example.carsharing.dto.payment;

import com.example.carsharing.model.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequestDto(
        @NotNull
        @Positive
        Long rentalId,

        @NotNull
        PaymentType type
) {}
