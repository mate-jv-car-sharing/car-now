package com.example.carsharing.service.payment;

import com.example.carsharing.dto.payment.CreatePaymentRequestDto;
import com.example.carsharing.dto.payment.PaymentResponseDto;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.mapper.PaymentMapper;
import com.example.carsharing.model.Payment;
import com.example.carsharing.model.Rental;
import com.example.carsharing.model.User;
import com.example.carsharing.model.enums.PaymentStatus;
import com.example.carsharing.model.enums.PaymentType;
import com.example.carsharing.repository.PaymentRepository;
import com.example.carsharing.repository.RentalRepository;
import com.example.carsharing.service.user.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final BigDecimal FINE_MULTIPLIER = BigDecimal.valueOf(2);

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final StripeService stripeService;
    private final PaymentMapper paymentMapper;
    private final UserService userService;

    @Value("${app.payment.success-url}")
    private String successUrl;

    @Value("${app.payment.cancel-url}")
    private String cancelUrl;

    @Override
    public PaymentResponseDto create(CreatePaymentRequestDto request, User user) {
        Rental rental = rentalRepository.findById(request.rentalId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id "
                        + request.rentalId())
        );
        BigDecimal amountToPay = calculateAmount(rental, request.type());
        try {
            String successSessionUrl = buildSuccessUrl(rental.getId(), request.type());
            String cancelSessionUrl = buildCancelUrl(rental.getId());
            Session session = stripeService.createCheckoutSession(
                    "Car Rental Payment",
                    amountToPay,
                    successSessionUrl,
                    cancelSessionUrl
            );
            Payment payment = new Payment();
            payment.setRental(rental);
            payment.setType(request.type());
            payment.setAmountToPay(amountToPay);
            payment.setSessionId(session.getId());
            payment.setSessionUrl(session.getUrl());
            return paymentMapper.toDto(paymentRepository.save(payment));
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe session", e);
        }
    }

    @Override
    public PaymentResponseDto getById(Long paymentId, User user) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment with id " + paymentId));
        boolean isManager = userService.isManager(user);
        if (!payment.getRental().getUser().getId().equals(user.getId())
                && !isManager) {
            throw new RuntimeException(String.format(
                    "Access denied for payment with id %d and user with id %d",
                    paymentId,
                    user.getId()));
        }
        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentResponseDto markAsPaid(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment with id " + sessionId)
        );
        payment.setStatus(PaymentStatus.PAID);
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public Page<PaymentResponseDto> getAllByUser(
            User user,
            Long requestedUserId,
            Pageable pageable
    ) {
        boolean isManager = userService.isManager(user);
        Page<Payment> payments;
        if (isManager) {
            if (requestedUserId != null) {
                payments = paymentRepository.findAllByRentalUserId(requestedUserId, pageable);
            } else {
                payments = paymentRepository.findAll(pageable);
            }
        } else {
            payments = paymentRepository.findAllByRentalUserId(user.getId(), pageable);
        }
        return payments.map(paymentMapper::toDto);
    }

    private BigDecimal calculateAmount(Rental rental, PaymentType type) {
        BigDecimal dailyFee = rental.getCar().getDailyFee();
        long rentalDays = ChronoUnit.DAYS.between(
                rental.getRentalDate(),
                rental.getReturnDate()
        );
        BigDecimal baseAmount = dailyFee.multiply(BigDecimal.valueOf(rentalDays));

        if (type == PaymentType.PAYMENT) {
            return baseAmount;
        }
        if (type == PaymentType.FINE) {
            if (rental.getActualReturnDate() == null) {
                throw new IllegalStateException(
                        "Fine can be created only after rental is returned"
                );
            }
            long overdueDays = ChronoUnit.DAYS.between(
                    rental.getReturnDate(),
                    rental.getActualReturnDate()
            );
            if (overdueDays <= 0) {
                return baseAmount;
            }
            BigDecimal overdueAmount = dailyFee
                    .multiply(BigDecimal.valueOf(overdueDays))
                    .multiply(FINE_MULTIPLIER);
            return baseAmount.add(overdueAmount);
        }
        throw new IllegalArgumentException("Unknown payment type");
    }

    private String buildSuccessUrl(Long rentalId, PaymentType type) {
        return UriComponentsBuilder.fromUriString(successUrl)
                .queryParam("rentalId", rentalId)
                .queryParam("type", type)
                .toUriString();
    }

    private String buildCancelUrl(Long rentalId) {
        return UriComponentsBuilder.fromUriString(cancelUrl)
                .queryParam("rentalId", rentalId)
                .toUriString();
    }
}
