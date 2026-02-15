package com.example.carsharing.service.payment;

import com.example.carsharing.dto.payment.CreatePaymentRequestDto;
import com.example.carsharing.dto.payment.PaymentResponseDto;
import com.example.carsharing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponseDto create(CreatePaymentRequestDto request, User user);

    PaymentResponseDto getById(Long id, User user);

    PaymentResponseDto markAsPaid(String sessionId);

    Page<PaymentResponseDto> getAllByUser(User user, Long requestedUserId, Pageable pageable);
}
