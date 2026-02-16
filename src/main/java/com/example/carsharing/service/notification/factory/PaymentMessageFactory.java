package com.example.carsharing.service.notification.factory;

import com.example.carsharing.model.Payment;

public class PaymentMessageFactory {

    public static String successfulPayment(Payment payment) {
        return String.format("""
        New payment was paid:
        - Payment id: %s
        - Rental id: %s
        - Customer: %s
        - Type: %s
        - Amount: %s
        """,
                payment.getId(),
                payment.getRental().getId(),
                payment.getRental().getUser().getEmail(),
                payment.getType(),
                payment.getAmountToPay());
    }
}
