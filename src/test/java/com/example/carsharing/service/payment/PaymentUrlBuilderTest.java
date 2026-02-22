package com.example.carsharing.service.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.carsharing.model.Rental;
import com.example.carsharing.model.enums.PaymentType;
import com.example.carsharing.util.RentalTestDataFactory;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class PaymentUrlBuilderTest {

    private PaymentUrlBuilder paymentUrlBuilder;

    @BeforeEach
    void setUp() {
        paymentUrlBuilder = new PaymentUrlBuilder();
        ReflectionTestUtils.setField(
                paymentUrlBuilder,
                "successUrl",
                "http://localhost:8080/success"
        );
        ReflectionTestUtils.setField(
                paymentUrlBuilder,
                "cancelUrl",
                "http://localhost:8080/cancel"
        );
    }

    @Test
    void buildUrls_shouldAppendRentalIdAndTypeToSuccessUrl() {
        Rental rental = RentalTestDataFactory.getSavedRental();

        Map<String, String> result = paymentUrlBuilder.buildUrls(rental, PaymentType.PAYMENT);

        UriComponents success = UriComponentsBuilder.fromUriString(result.get("successUrl"))
                .build();
        assertEquals("http", success.getScheme());
        assertEquals("localhost", success.getHost());
        assertEquals(8080, success.getPort());
        assertEquals("/success", success.getPath());

        MultiValueMap<String, String> successParams = success.getQueryParams();
        assertEquals("1", successParams.getFirst("rentalId"));
        assertEquals("PAYMENT", successParams.getFirst("type"));

        UriComponents cancel = UriComponentsBuilder.fromUriString(result.get("cancelUrl"))
                .build();
        assertEquals("/cancel", cancel.getPath());

        MultiValueMap<String, String> cancelParams = cancel.getQueryParams();
        assertEquals("1", cancelParams.getFirst("rentalId"));
        assertNull(cancelParams.getFirst("type"));
    }
}
