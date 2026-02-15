package com.example.carsharing.controller;

import com.example.carsharing.dto.payment.CreatePaymentRequestDto;
import com.example.carsharing.dto.payment.PaymentResponseDto;
import com.example.carsharing.model.User;
import com.example.carsharing.service.payment.PaymentService;
import jakarta.validation.Valid;
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
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @PostMapping
    public PaymentResponseDto createPayment(
            @Valid @RequestBody CreatePaymentRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        return paymentService.create(requestDto, user);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @GetMapping("/{id}")
    public PaymentResponseDto getPaymentById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return paymentService.getById(id, user);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @GetMapping("/success")
    public PaymentResponseDto paymentSuccess(@RequestParam("session_id") String sessionId) {
        return paymentService.markAsPaid(sessionId);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @GetMapping("/cancel")
    public String paymentCancel(@RequestParam("rentalId") Long rentalId) {
        return "Payment for rental " + rentalId + " has been canceled. "
                + "Try again later this day, please.";
    }

    @GetMapping
    public Page<PaymentResponseDto> getPayments(
            @RequestParam(value = "user_id", required = false) Long userId,
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return paymentService.getAllByUser(user, userId, pageable);
    }
}
