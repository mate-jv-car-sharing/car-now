package com.example.carsharing.service.payment;

import com.example.carsharing.dto.payment.CreatePaymentRequestDto;
import com.example.carsharing.dto.payment.PaymentResponseDto;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.exception.PaymentException;
import com.example.carsharing.mapper.PaymentMapper;
import com.example.carsharing.model.Payment;
import com.example.carsharing.model.Rental;
import com.example.carsharing.model.User;
import com.example.carsharing.model.enums.PaymentStatus;
import com.example.carsharing.model.enums.PaymentType;
import com.example.carsharing.repository.PaymentRepository;
import com.example.carsharing.repository.RentalRepository;
import com.example.carsharing.service.RentalAccessValidator;
import com.example.carsharing.service.user.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.SessionStatus;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final BigDecimal FINE_MULTIPLIER = BigDecimal.valueOf(2);
    private static final String SESSION_STATUS_PAID = "paid";

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final StripeService stripeService;
    private final PaymentMapper paymentMapper;
    private final UserService userService;
    private final RentalAccessValidator rentalAccessValidator;
    private final PaymentUrlBuilder paymentUrlBuilder;

    @Override
    public PaymentResponseDto create(CreatePaymentRequestDto request, User user) {
        Rental rental = rentalRepository.findById(request.rentalId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find rental with id " + request.rentalId()
                ));

        if (!userService.isManager(user)) {
            rentalAccessValidator.validateRentalOwnership(user, rental);
        }

        BigDecimal amountToPay = calculateAmount(rental, request.type());
        Map<String, String> urls = paymentUrlBuilder.buildUrls(rental, request.type());
        try {
            Session session = stripeService.createCheckoutSession(
                    "Car Rental Payment",
                    amountToPay,
                    urls.get("successUrl"),
                    urls.get("cancelUrl")
            );
            Payment payment = createPayment(request, rental, amountToPay, session);
            return paymentMapper.toDto(paymentRepository.save(payment));
        } catch (StripeException e) {
            throw new PaymentException("Failed to create Stripe session for rental with id "
                    + rental.getId(), e);
        }
    }

    @Override
    public PaymentResponseDto getById(Long paymentId, User user) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment with id " + paymentId));

        if (!rentalAccessValidator.isRentalOwner(user, payment.getRental())
                && !userService.isManager(user)) {
            throw new AccessDeniedException(String.format(
                    "Access denied for payment with id %d and user with id %d",
                    paymentId,
                    user.getId()));
        }
        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentResponseDto markAsPaid(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Can't find payment with session id " + sessionId)
        );
        if (payment.getStatus() == PaymentStatus.PAID) {
            return paymentMapper.toDto(payment);
        }
        try {
            Session session = stripeService.retrieveSession(sessionId);
            if (!SESSION_STATUS_PAID.equals(session.getPaymentStatus())) {
                throw new PaymentException("Payment is not completed in Stripe. Status: "
                        + session.getPaymentStatus());
            }
            payment.setStatus(PaymentStatus.PAID);
            return paymentMapper.toDto(paymentRepository.save(payment));
        } catch (StripeException e) {
            throw new PaymentException(
                    "Failed to verify payment with Stripe: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<PaymentResponseDto> getAllByUser(
            User user,
            Long requestUserId,
            Pageable pageable
    ) {
        Long userIdToQuery = userService.isManager(user)
                ? requestUserId
                : user.getId();
        return (userIdToQuery != null
                ? paymentRepository.findAllByRentalUserId(userIdToQuery, pageable)
                : paymentRepository.findAll(pageable))
                .map(paymentMapper::toDto);
    }

    private Payment createPayment(
            CreatePaymentRequestDto request,
            Rental rental,
            BigDecimal amountToPay,
            Session session) {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setType(request.type());
        payment.setAmountToPay(amountToPay);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());
        return payment;
    }

    private BigDecimal calculateBaseAmount(Rental rental) {
        BigDecimal dailyFee = rental.getCar().getDailyFee();
        long rentalDays = ChronoUnit.DAYS.between(
                rental.getRentalDate(),
                rental.getReturnDate()
        );
        return dailyFee.multiply(BigDecimal.valueOf(rentalDays));
    }

    private BigDecimal calculateFineAmount(Rental rental) {
        if (rental.getActualReturnDate() == null) {
            throw new PaymentException(
                    "Fine can be created only after rental is returned. "
                    + "Rental with id " + rental.getId() + " is still active."
            );
        }
        long overdueDays = ChronoUnit.DAYS.between(
                rental.getReturnDate(),
                rental.getActualReturnDate()
        );
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }
        return rental.getCar().getDailyFee()
                .multiply(BigDecimal.valueOf(overdueDays))
                .multiply(FINE_MULTIPLIER);
    }

    private BigDecimal calculateAmount(Rental rental, PaymentType type) {
        BigDecimal baseAmount = calculateBaseAmount(rental);
        return switch (type) {
            case PAYMENT -> baseAmount;
            case FINE -> baseAmount.add(calculateFineAmount(rental));
        };
    }
}
