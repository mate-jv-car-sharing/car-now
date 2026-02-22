package com.example.carsharing.util;

import com.example.carsharing.dto.payment.PaymentResponseDto;
import com.example.carsharing.model.Payment;
import com.example.carsharing.model.Rental;
import com.example.carsharing.model.enums.PaymentStatus;
import com.example.carsharing.model.enums.PaymentType;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

public class PaymentTestDataFactory {

    private static final String DEFAULT_SESSION_ID = "sess_123";
    private static final String DEFAULT_SESSION_URL = "https://stripe-session-url";
    private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(600);

    public static Payment getPayment(Rental rental) {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setType(PaymentType.PAYMENT);
        payment.setAmountToPay(DEFAULT_AMOUNT);
        payment.setSessionId(DEFAULT_SESSION_ID);
        payment.setSessionUrl(DEFAULT_SESSION_URL);
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    public static Payment getSavedPayment(Rental rental) {
        Payment payment = getPayment(rental);
        ReflectionTestUtils.setField(payment, "id", 1L);
        return payment;
    }

    public static Payment getSavedPaidPayment(Rental rental) {
        Payment payment = getSavedPayment(rental);
        payment.setStatus(PaymentStatus.PAID);
        return payment;
    }

    public static Payment getSavedFinePayment(Rental rental, BigDecimal amount) {
        Payment payment = getSavedPayment(rental);
        payment.setType(PaymentType.FINE);
        payment.setAmountToPay(amount);
        return payment;
    }

    public static PaymentResponseDto getPaymentResponseDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getStatus(),
                payment.getType(),
                payment.getAmountToPay(),
                payment.getSessionUrl(),
                payment.getSessionId()
        );
    }
}