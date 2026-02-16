package com.example.carsharing.dto.payment;

import com.example.carsharing.model.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request to create a payment session")
public record CreatePaymentRequestDto(
        @Schema(description = "Rental ID", example = "1")
        @NotNull
        @Positive
        Long rentalId,

        @Schema(description = "Payment type", example = "PAYMENT")
        @NotNull
        PaymentType type
) {}
