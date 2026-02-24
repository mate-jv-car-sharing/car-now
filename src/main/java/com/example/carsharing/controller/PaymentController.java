package com.example.carsharing.controller;

import com.example.carsharing.dto.payment.CreatePaymentRequestDto;
import com.example.carsharing.dto.payment.PaymentResponseDto;
import com.example.carsharing.model.User;
import com.example.carsharing.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management and Stripe integration")
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "Create a payment session",
            description = "Creates Stripe checkout session for rental"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @PostMapping
    public PaymentResponseDto createPayment(
            @Valid @RequestBody CreatePaymentRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        return paymentService.create(requestDto, user);
    }

    @Operation(summary = "Get payment by ID")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @GetMapping("/{id}")
    public PaymentResponseDto getPaymentById(
            @Positive @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return paymentService.getById(id, user);
    }

    @Operation(
            summary = "Handle successful payment",
            description = "Stripe redirects here after successful payment"
    )
    @GetMapping("/success")
    public PaymentResponseDto paymentSuccess(@RequestParam("sessionId") String sessionId) {
        return paymentService.markAsPaid(sessionId);
    }

    @Operation(
            summary = "Handle cancelled payment",
            description = "Stripe redirects here when payment is cancelled"
    )
    @GetMapping("/cancel")
    public String paymentCancel(@Positive @RequestParam("rentalId") Long rentalId) {
        return "Payment for rental " + rentalId + " has been canceled. "
                + "Try again later this day, please.";
    }

    @Operation(
            summary = "Get all payments",
            description = "Customers see only their payments, managers can filter by user"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @GetMapping
    public Page<PaymentResponseDto> getPayments(
            @Positive @RequestParam(value = "userId", required = false) Long userId,
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return paymentService.getAllByUser(user, userId, pageable);
    }
}
