package com.example.carsharing.service.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.carsharing.dto.payment.CreatePaymentRequestDto;
import com.example.carsharing.dto.payment.PaymentResponseDto;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.exception.PaymentException;
import com.example.carsharing.exception.RentalException;
import com.example.carsharing.mapper.PaymentMapper;
import com.example.carsharing.model.Payment;
import com.example.carsharing.model.Rental;
import com.example.carsharing.model.User;
import com.example.carsharing.model.enums.PaymentStatus;
import com.example.carsharing.model.enums.PaymentType;
import com.example.carsharing.repository.PaymentRepository;
import com.example.carsharing.repository.RentalRepository;
import com.example.carsharing.service.RentalAccessValidator;
import com.example.carsharing.service.notification.NotificationService;
import com.example.carsharing.service.user.UserService;
import com.example.carsharing.util.PaymentTestDataFactory;
import com.example.carsharing.util.RentalTestDataFactory;
import com.example.carsharing.util.UserTestDataFactory;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    private static final Long DEFAULT_ID = 1L;
    private static final Long DEFAULT_DAYS = 5L;
    public static final int OVERDUE_DAYS = 2;
    public static final int FINE_MULTIPLIER = 2;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private StripeService stripeService;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private UserService userService;
    @Mock
    private RentalAccessValidator rentalAccessValidator;
    @Mock
    private PaymentUrlBuilder paymentUrlBuilder;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Creates payment and returns PaymentResponseDto when user is manager and rental exists")
    void create_whenUserIsManager_returnsPaymentResponse() throws Exception {
        User manager = UserTestDataFactory.getUserWithManagerRoleAndId();
        Rental rental = RentalTestDataFactory.getSavedRental();
        CreatePaymentRequestDto request =
                new CreatePaymentRequestDto(rental.getId(), PaymentType.PAYMENT);
        BigDecimal expectedAmount = getBaseAmount(rental);
        Payment savedPayment = PaymentTestDataFactory.getSavedPayment(rental);
        PaymentResponseDto expectedResponse = PaymentTestDataFactory.getPaymentResponseDto(savedPayment);

        when(userService.isManager(manager)).thenReturn(true);
        when(paymentMapper.toDto(savedPayment))
                .thenReturn(expectedResponse);
        mockSuccessfulPaymentFlow(
                rental,
                request.type(),
                savedPayment,
                expectedAmount
        );

        PaymentResponseDto actualResponse = paymentService.create(request, manager);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verifySuccessfulPaymentFlow(
                request,
                rental,
                expectedAmount,
                savedPayment,
                manager,
                true
        );
    }

    @Test
    @DisplayName("Creates payment and returns PaymentResponseDto when user is customer and owner of rental")
    void create_whenUserIsCustomerAndOwner_returnsPaymentResponseDto() throws Exception {
        User customer = UserTestDataFactory.getUserWithCustomerRoleAndId();
        Rental rental = RentalTestDataFactory.getSavedRental();
        CreatePaymentRequestDto request =
                new CreatePaymentRequestDto(rental.getId(), PaymentType.PAYMENT);
        BigDecimal expectedAmount = getBaseAmount(rental);
        Payment savedPayment = PaymentTestDataFactory.getSavedPayment(rental);
        PaymentResponseDto expectedResponse = PaymentTestDataFactory.getPaymentResponseDto(savedPayment);

        when(userService.isManager(customer)).thenReturn(false);
        when(paymentMapper.toDto(savedPayment))
                .thenReturn(expectedResponse);
        mockSuccessfulPaymentFlow(
                rental,
                request.type(),
                savedPayment,
                expectedAmount
        );

        PaymentResponseDto actualResponse = paymentService.create(request, customer);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verifySuccessfulPaymentFlow(
                request,
                rental,
                expectedAmount,
                savedPayment,
                customer,
                false
        );
    }

    @Test
    @DisplayName("Creates fine payment and returns PaymentResponseDto when user is manager and rental is overdue")
    void create_whenPaymentTypeIsFineAndRentalIsOverdue_returnsPaymentResponseDto() throws Exception {
        User manager = UserTestDataFactory.getUserWithManagerRoleAndId();
        Rental rental = RentalTestDataFactory.getOverdueRental();
        CreatePaymentRequestDto request =
                new CreatePaymentRequestDto(rental.getId(), PaymentType.FINE);
        BigDecimal dailyFee = rental.getCar().getDailyFee();
        BigDecimal baseAmount = getBaseAmount(rental);
        BigDecimal fineAmount = dailyFee
                .multiply(BigDecimal.valueOf(OVERDUE_DAYS))
                .multiply(BigDecimal.valueOf(FINE_MULTIPLIER));
        BigDecimal expectedAmount = baseAmount.add(fineAmount);

        Payment savedPayment = PaymentTestDataFactory.getSavedFinePayment(rental, expectedAmount);
        PaymentResponseDto expectedResponse = PaymentTestDataFactory.getPaymentResponseDto(savedPayment);

        when(userService.isManager(manager)).thenReturn(true);
        when(paymentMapper.toDto(savedPayment))
                .thenReturn(expectedResponse);

        mockSuccessfulPaymentFlow(
                rental,
                request.type(),
                savedPayment,
                expectedAmount
        );

        PaymentResponseDto actualResponse = paymentService.create(request, manager);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verifySuccessfulPaymentFlow(
                request,
                rental,
                expectedAmount,
                savedPayment,
                manager,
                true
        );

    }

    @Test
    @DisplayName("Throws EntityNotFoundException when rental is not found in DB")
    void create_whenRentalNotFound_throwsEntityNotFoundException() throws Exception {
        User manager = UserTestDataFactory.getUserWithManagerRoleAndId();
        Long nonExistentRentalId = 666L;
        CreatePaymentRequestDto request =
                new CreatePaymentRequestDto(nonExistentRentalId, PaymentType.PAYMENT);

        when(rentalRepository.findById(nonExistentRentalId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.create(request, manager)
        );

        assertEquals("Can't find rental with id " + request.rentalId(), exception.getMessage());

        verify(rentalRepository).findById(nonExistentRentalId);
        verifyNoInteractions(
                rentalAccessValidator,
                userService,
                paymentUrlBuilder,
                stripeService,
                paymentRepository,
                paymentMapper
        );
    }

    @Test
    @DisplayName("Throws RentalException when user is not manager and not owner of rental")
    void create_whenUserIsNotManagerAndNotOwner_throwsRentalException() throws Exception {
        User customer = UserTestDataFactory.getUserWithCustomerRoleAndId();
        Rental rental = RentalTestDataFactory.getSavedRental();
        CreatePaymentRequestDto request =
                new CreatePaymentRequestDto(rental.getId(), PaymentType.PAYMENT);

        when(rentalRepository.findById(rental.getId()))
                .thenReturn(Optional.of(rental));
        when(userService.isManager(customer)).thenReturn(false);
        doThrow(new RentalException("User is not the owner of the rental"))
                .when(rentalAccessValidator)
                .validateRentalOwnership(customer, rental);

        RentalException exception = assertThrows(
                RentalException.class,
                () -> paymentService.create(request, customer)
        );

        assertEquals(
                "User is not the owner of the rental",
                exception.getMessage()
        );

        verify(rentalRepository).findById(rental.getId());
        verify(userService).isManager(customer);
        verify(rentalAccessValidator).validateRentalOwnership(customer, rental);
        verifyNoInteractions(
                paymentUrlBuilder,
                stripeService,
                paymentRepository,
                paymentMapper
        );
    }

    @Test
    @DisplayName("Throws PaymentException when Stripe session creation fails")
    void create_whenStripeThrowsStripeException_throwsPaymentException() throws Exception {
        User manager = UserTestDataFactory.getUserWithManagerRoleAndId();
        Rental rental = RentalTestDataFactory.getSavedRental();
        CreatePaymentRequestDto request =
                new CreatePaymentRequestDto(rental.getId(), PaymentType.PAYMENT);
        BigDecimal expectedAmount = rental.getCar().getDailyFee()
                .multiply(BigDecimal.valueOf(DEFAULT_DAYS));
        when(rentalRepository.findById(rental.getId()))
                .thenReturn(Optional.of(rental));
        when(userService.isManager(manager)).thenReturn(true);
        when(paymentUrlBuilder.buildUrls(rental, request.type()))
                .thenReturn(Map.of(
                        "successUrl", "success-url",
                        "cancelUrl", "cancel-url"
                ));

        StripeException stripeException = mock(StripeException.class);
        when(stripeService.createCheckoutSession(
                anyString(),
                eq(expectedAmount),
                eq("success-url"),
                eq("cancel-url")
        )).thenThrow(stripeException);

        PaymentException ex = assertThrows(
                PaymentException.class,
                () -> paymentService.create(request, manager)
        );

        assertTrue(ex.getMessage().contains(
                "Failed to create Stripe session for rental with id " + rental.getId()
        ));
        assertSame(stripeException, ex.getCause());

        verify(rentalRepository).findById(rental.getId());
        verify(userService).isManager(manager);
        verify(rentalAccessValidator, never())
                .validateRentalOwnership(any(), any());
        verify(paymentUrlBuilder).buildUrls(rental, request.type());
        verify(stripeService).createCheckoutSession(
                anyString(),
                eq(expectedAmount),
                eq("success-url"),
                eq("cancel-url")
        );
        verifyNoInteractions(paymentRepository, paymentMapper);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("Throws PaymentException when creating fine for active rental")
    void create_whenPaymentTypeIsFineAndRentalIsActive_throwsPaymentException() {
        User manager = UserTestDataFactory.getUserWithManagerRoleAndId();
        Rental activeRental = RentalTestDataFactory.getActiveRental();
        CreatePaymentRequestDto request =
                new CreatePaymentRequestDto(activeRental.getId(), PaymentType.FINE);

        when(rentalRepository.findById(activeRental.getId()))
                .thenReturn(Optional.of(activeRental));
        when(userService.isManager(manager)).thenReturn(true);

        PaymentException ex = assertThrows(
                PaymentException.class,
                () -> paymentService.create(request, manager)
        );

        assertTrue(ex.getMessage().contains("Fine can be created only after rental is returned"));

        verify(rentalRepository).findById(activeRental.getId());
        verify(userService).isManager(manager);
        verify(rentalAccessValidator, never())
                .validateRentalOwnership(any(), any());
        verifyNoInteractions(
                paymentUrlBuilder,
                stripeService,
                paymentRepository,
                paymentMapper,
                notificationService
        );
    }

    @Test
    @DisplayName("Returns PaymentResponseDto when payment exists and user is rental owner")
    void getById_whenUserIsOwner_returnsPaymentResponseDto() {
        User customer = UserTestDataFactory.getUserWithCustomerRoleAndId();
        Rental rental = RentalTestDataFactory.getSavedRental();
        Payment payment = PaymentTestDataFactory.getSavedPayment(rental);
        PaymentResponseDto expectedResponse = PaymentTestDataFactory.getPaymentResponseDto(payment);

        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(rentalAccessValidator.isRentalOwner(customer, rental)).thenReturn(true);
        when(paymentMapper.toDto(payment)).thenReturn(expectedResponse);

        PaymentResponseDto actualResponse = paymentService.getById(payment.getId(), customer);

        assertEquals(expectedResponse, actualResponse);

        verify(paymentRepository).findById(payment.getId());
        verify(rentalAccessValidator).isRentalOwner(customer, rental);
        verify(paymentMapper).toDto(payment);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Returns PaymentResponseDto when payment exists and user is manager")
    void getById_whenUserIsManager_returnsPaymentResponseDto() {
        User customer = UserTestDataFactory.getUserWithManagerRoleAndId();
        Rental rental = RentalTestDataFactory.getSavedRental();
        Payment payment = PaymentTestDataFactory.getSavedPayment(rental);
        PaymentResponseDto expectedResponse = PaymentTestDataFactory.getPaymentResponseDto(payment);

        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(rentalAccessValidator.isRentalOwner(customer, rental)).thenReturn(false);
        when(userService.isManager(customer)).thenReturn(true);
        when(paymentMapper.toDto(payment)).thenReturn(expectedResponse);

        PaymentResponseDto actualResponse = paymentService.getById(payment.getId(), customer);

        assertEquals(expectedResponse, actualResponse);

        verify(paymentRepository).findById(payment.getId());
        verify(rentalAccessValidator).isRentalOwner(customer, rental);
        verify(paymentMapper).toDto(payment);
        verify(userService).isManager(customer);
    }

    @Test
    @DisplayName("Throws EntityNotFoundException when payment is not found")
    void getById_whenPaymentNotFound_throwsEntityNotFoundException() {
        User user = UserTestDataFactory.getUserWithCustomerRoleAndId();
        Long paymentId = 666L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.getById(paymentId, user)
        );

        assertEquals("Can't find payment with id " + paymentId, ex.getMessage());

        verify(paymentRepository).findById(paymentId);
        verifyNoInteractions(rentalAccessValidator, userService, paymentMapper);
    }

    @Test
    @DisplayName("Throws AccessDeniedException when user is not owner and not manager")
    void getById_whenUserNotOwnerAndNotManager_throwsAccessDeniedException() {
        User customer = UserTestDataFactory.getUserWithCustomerRoleAndId();
        Rental rental = RentalTestDataFactory.getSavedRental();
        Payment payment = PaymentTestDataFactory.getSavedPayment(rental);

        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(rentalAccessValidator.isRentalOwner(customer, rental)).thenReturn(false);
        when(userService.isManager(customer)).thenReturn(false);

        org.springframework.security.access.AccessDeniedException ex = assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> paymentService.getById(payment.getId(), customer)
        );

        assertEquals(
                String.format("Access denied for payment with id %d and user with id %d",
                        payment.getId(), customer.getId()),
                ex.getMessage()
        );

        verify(paymentRepository).findById(payment.getId());
        verify(rentalAccessValidator).isRentalOwner(customer, rental);
        verify(userService).isManager(customer);
        verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Returns dto without Stripe call when payment already PAID")
    void markAsPaid_whenPaymentAlreadyPaid_returnsDto() {
        Rental rental = RentalTestDataFactory.getSavedRental();
        Payment paidPayment = PaymentTestDataFactory.getSavedPaidPayment(rental);
        String sessionId = paidPayment.getSessionId();

        PaymentResponseDto expected = PaymentTestDataFactory.getPaymentResponseDto(paidPayment);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(paidPayment));
        when(paymentMapper.toDto(paidPayment)).thenReturn(expected);

        PaymentResponseDto actual = paymentService.markAsPaid(sessionId);

        assertEquals(expected, actual);

        verify(paymentRepository).findBySessionId(sessionId);
        verify(paymentMapper).toDto(paidPayment);
        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(stripeService, notificationService);
    }

    @Test
    @DisplayName("Marks payment as PAID, sends notification and returns dto when Stripe paymentStatus is paid")
    void markAsPaid_whenStripePaid_marksAsPaidAndSavesAndNotifies() throws Exception {
        Rental rental = RentalTestDataFactory.getSavedRental();
        Payment pendingPayment = PaymentTestDataFactory.getSavedPayment(rental); // PENDING
        String sessionId = pendingPayment.getSessionId();
        Session session = mock(Session.class);
        when(session.getPaymentStatus()).thenReturn("paid");
        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(pendingPayment));
        when(stripeService.retrieveSession(sessionId)).thenReturn(session);
        when(paymentRepository.save(pendingPayment)).thenReturn(pendingPayment);
        PaymentResponseDto expected = PaymentTestDataFactory.getPaymentResponseDto(pendingPayment);
        when(paymentMapper.toDto(pendingPayment)).thenReturn(expected);
        when(session.getId()).thenReturn(sessionId);

        PaymentResponseDto actual = paymentService.markAsPaid(sessionId);

        assertEquals(expected, actual);
        assertEquals(PaymentStatus.PAID, pendingPayment.getStatus());

        verify(paymentRepository).findBySessionId(sessionId);
        verify(stripeService).retrieveSession(sessionId);
        verify(paymentRepository).save(pendingPayment);
        verify(notificationService).sendMessage(anyString());
        verify(paymentMapper).toDto(pendingPayment);
    }

    @Test
    @DisplayName("Returns user's payments when user is not manager")
    void getAllByUser_whenUserIsNotManager_queriesByUserId() {
        User customer = UserTestDataFactory.getUserWithCustomerRoleAndId();
        Pageable pageable = PageRequest.of(0, 10);
        Rental rental = RentalTestDataFactory.getSavedRental();
        Payment payment = PaymentTestDataFactory.getSavedPayment(rental);
        Page<Payment> paymentsPage = new PageImpl<>(List.of(payment), pageable, 1);
        PaymentResponseDto dto = PaymentTestDataFactory.getPaymentResponseDto(payment);

        when(userService.isManager(customer)).thenReturn(false);
        when(paymentRepository.findAllByRentalUserId(customer.getId(), pageable))
                .thenReturn(paymentsPage);
        when(paymentMapper.toDto(payment)).thenReturn(dto);

        Page<PaymentResponseDto> result = paymentService.getAllByUser(customer, 999L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().getFirst());

        verify(userService).isManager(customer);
        verify(paymentRepository).findAllByRentalUserId(customer.getId(), pageable);
        verify(paymentMapper).toDto(payment);
        verify(paymentRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Returns all payments when user is manager and requestUserId is null")
    void getAllByUser_whenManagerAndRequestUserIdNull_queriesAll() {
        User manager = UserTestDataFactory.getUserWithManagerRoleAndId();
        Pageable pageable = PageRequest.of(0, 10);
        Rental rental = RentalTestDataFactory.getSavedRental();
        Payment payment = PaymentTestDataFactory.getSavedPayment(rental);
        Page<Payment> paymentsPage = new PageImpl<>(List.of(payment), pageable, 1);
        PaymentResponseDto dto = PaymentTestDataFactory.getPaymentResponseDto(payment);

        when(userService.isManager(manager)).thenReturn(true);
        when(paymentRepository.findAll(pageable)).thenReturn(paymentsPage);
        when(paymentMapper.toDto(payment)).thenReturn(dto);

        Page<PaymentResponseDto> result = paymentService.getAllByUser(manager, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().getFirst());

        verify(userService).isManager(manager);
        verify(paymentRepository).findAll(pageable);
        verify(paymentMapper).toDto(payment);
        verify(paymentRepository, never()).findAllByRentalUserId(anyLong(), any(Pageable.class));
    }

    private BigDecimal getBaseAmount(Rental rental) {
        return rental.getCar().getDailyFee().multiply(BigDecimal.valueOf(DEFAULT_DAYS));
    }

    private CreatePaymentRequestDto request(Rental rental, PaymentType type) {
        return new CreatePaymentRequestDto(rental.getId(), type);
    }

    private void mockSuccessfulPaymentFlow(
            Rental rental,
            PaymentType paymentType,
            Payment savedPayment,
            BigDecimal expectedAmount
    ) throws Exception {
        Session session = mock(Session.class);
        when(rentalRepository.findById(rental.getId()))
                .thenReturn(Optional.of(rental));
        when(paymentUrlBuilder.buildUrls(rental, paymentType))
                .thenReturn(Map.of(
                        "successUrl", "success-url",
                        "cancelUrl", "cancel-url"
                ));
        when(stripeService.createCheckoutSession(
                anyString(),
                eq(expectedAmount),
                eq("success-url"),
                eq("cancel-url")
        )).thenReturn(session);
        when(session.getId()).thenReturn(savedPayment.getSessionId());
        when(session.getUrl()).thenReturn(savedPayment.getSessionUrl());
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);
    }

    private void verifySuccessfulPaymentFlow(
            CreatePaymentRequestDto request,
            Rental rental,
            BigDecimal expectedAmount,
            Payment savedPayment,
            User user,
            boolean isManager
    ) throws Exception {
        verify(rentalRepository).findById(request.rentalId());
        verify(userService).isManager(user);

        if (isManager) {
            verify(rentalAccessValidator, never()).validateRentalOwnership(any(), any());
        } else {
            verify(rentalAccessValidator).validateRentalOwnership(user, rental);
        }

        verify(paymentUrlBuilder).buildUrls(rental, request.type());
        verify(stripeService).createCheckoutSession(
                anyString(),
                eq(expectedAmount),
                eq("success-url"),
                eq("cancel-url")
        );
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentMapper).toDto(savedPayment);
    }
}
