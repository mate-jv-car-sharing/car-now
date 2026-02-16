package com.example.carsharing.dto.payment;

import com.example.carsharing.model.enums.PaymentStatus;
import com.example.carsharing.model.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Payment information")
public record PaymentResponseDto(
        @Schema(description = "Payment ID", example = "1")
        Long id,

        @Schema(description = "Payment status", example = "PENDING")
        PaymentStatus status,

        @Schema(description = "Payment type", example = "PAYMENT")
        PaymentType type,

        @Schema(description = "Amount to pay in USD", example = "150.00")
        BigDecimal amountToPay,

        @Schema(description = "Stripe session URL for payment")
        String sessionUrl,

        @Schema(description = "Stripe session ID")
        String sessionId
) {}
