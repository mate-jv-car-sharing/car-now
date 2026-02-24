package com.example.carsharing.controller;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharing.dto.payment.CreatePaymentRequestDto;
import com.example.carsharing.dto.payment.PaymentResponseDto;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.model.User;
import com.example.carsharing.model.enums.PaymentStatus;
import com.example.carsharing.model.enums.PaymentType;
import com.example.carsharing.service.payment.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {
    public static final String PAYMENTS_URL = "/payments";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("Returns 401 Unauthorized when user is not authenticated")
    void getPayments_WhenUserNotAuthenticated_ThenReturns401() throws Exception {
        mockMvc.perform(get(PAYMENTS_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Returns 403 Forbidden when user who is not manager/customer is trying to create a payment")
    void createPayment_WhenUserHasInvalidRole_ThenReturns403() throws Exception {
        User user = mockUser(1L);
        CreatePaymentRequestDto request = getValidCreatePaymentRequestDto();

        mockMvc.perform(post(PAYMENTS_URL)
                        .with(authentication(authWithRole(user, "ROLE_DRIVER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Returns 200 OK when a valid payment request is made")
    void createPayment_WhenValidRequest_ThenReturns200() throws Exception {
        User user = mockUser(1L);
        CreatePaymentRequestDto request = getValidCreatePaymentRequestDto();

        PaymentResponseDto response = new PaymentResponseDto(
                10L,
                PaymentStatus.PENDING,
                request.type(),
                BigDecimal.valueOf(150.00),
                "https://stripe.test/session-url",
                "sess_123"
        );

        when(paymentService.create(any(CreatePaymentRequestDto.class), any(User.class)))
                .thenReturn(response);

        mockMvc.perform(post(PAYMENTS_URL)
                        .with(authentication(authCustomer(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value(request.type().name()))
                .andExpect(jsonPath("$.amountToPay").value(150.00))
                .andExpect(jsonPath("$.sessionUrl").value("https://stripe.test/session-url"))
                .andExpect(jsonPath("$.sessionId").value("sess_123"));

        verify(paymentService).create(any(CreatePaymentRequestDto.class), eq(user));
    }

    @Test
    @DisplayName("Returns 400 Bad Request when an invalid payment request is made")
    void createPayment_WhenInvalidRequest_ThenReturns400() throws Exception {
        User user = mockUser(1L);

        CreatePaymentRequestDto invalidRequest = new CreatePaymentRequestDto(
                null,
                PaymentType.PAYMENT
        );

        mockMvc.perform(post(PAYMENTS_URL)
                        .with(authentication(authCustomer(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors").value(hasItem(containsString("rentalId"))));
    }

    @Test
    @DisplayName("Returns 200 OK when payment exists by id")
    void getPaymentById_WhenValidId_ThenReturns200() throws Exception {
        User user = mockUser(1L);
        Long id = 10L;

        PaymentResponseDto response = new PaymentResponseDto(
                id,
                PaymentStatus.PENDING,
                PaymentType.PAYMENT,
                BigDecimal.valueOf(150.00),
                "https://stripe.test/session-url",
                "sess_123"
        );

        when(paymentService.getById(eq(id), any(User.class))).thenReturn(response);

        mockMvc.perform(get(PAYMENTS_URL + "/{id}", id)
                        .with(authentication(authCustomer(user))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("PAYMENT"));

        verify(paymentService).getById(eq(id), any(User.class));
    }

    @Test
    @DisplayName("Returns 404 Not Found when payment does not exist")
    void getPaymentById_WhenPaymentNotFound_ThenReturns404() throws Exception {
        User user = mockUser(1L);
        long missingId = 999_999L;

        when(paymentService.getById(eq(missingId), any(User.class)))
                .thenThrow(new EntityNotFoundException("Can't find payment with id " + missingId));

        mockMvc.perform(get(PAYMENTS_URL + "/{id}", missingId)
                        .with(authentication(authCustomer(user))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Returns 200 OK when payment success callback is called with valid sessionId")
    void paymentSuccess_WhenValidSessionId_ThenReturns200() throws Exception {
        String sessionId = "sess_123";

        PaymentResponseDto response = new PaymentResponseDto(
                10L,
                PaymentStatus.PAID,
                PaymentType.PAYMENT,
                BigDecimal.valueOf(150.00),
                "https://stripe.test/session-url",
                sessionId
        );

        when(paymentService.markAsPaid(eq(sessionId))).thenReturn(response);

        mockMvc.perform(get(PAYMENTS_URL + "/success")
                        .param("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.sessionId").value(sessionId));

        verify(paymentService).markAsPaid(eq(sessionId));
    }

    @Test
    @DisplayName("Returns 200 OK when payment cancel callback is called with valid rentalId")
    void paymentCancel_WhenValidRentalId_ThenReturns200() throws Exception {
        long rentalId = 7L;

        mockMvc.perform(get(PAYMENTS_URL + "/cancel")
                        .param("rentalId", String.valueOf(rentalId)))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Payment for rental " + rentalId),
                        containsString("has been canceled")
                )));
    }

    @Test
    @DisplayName("Returns 200 OK when getting payments with default pageable")
    void getPayments_WhenDefaultPageable_ThenReturns200() throws Exception {
        User user = mockUser(1L);

        List<PaymentResponseDto> content = List.of(
                new PaymentResponseDto(1L, PaymentStatus.PENDING, PaymentType.PAYMENT,
                        BigDecimal.valueOf(100.00), "url1", "sess_1"),
                new PaymentResponseDto(2L, PaymentStatus.PAID, PaymentType.PAYMENT,
                        BigDecimal.valueOf(150.00), "url2", "sess_2")
        );

        when(paymentService.getAllByUser(any(User.class), any(), any()))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 20), 2));

        mockMvc.perform(get(PAYMENTS_URL)
                        .with(authentication(authCustomer(user)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))

                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));

        verify(paymentService).getAllByUser(any(User.class), any(), any());
    }

    private CreatePaymentRequestDto getValidCreatePaymentRequestDto() {
        return new CreatePaymentRequestDto(
                1L,
                PaymentType.PAYMENT
        );
    }

    private User mockUser(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UsernamePasswordAuthenticationToken authCustomer(User user) {
        return authWithRole(user, "ROLE_CUSTOMER");
    }

    private UsernamePasswordAuthenticationToken authWithRole(User user, String role) {
        return new UsernamePasswordAuthenticationToken(
                user,
                "N/A",
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
