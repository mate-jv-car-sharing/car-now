package com.example.carsharing.service.payment;

import com.example.carsharing.model.Rental;
import com.example.carsharing.model.enums.PaymentType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class PaymentUrlBuilder {
    @Value("${app.payment.success-url}")
    private String successUrl;

    @Value("${app.payment.cancel-url}")
    private String cancelUrl;

    public Map<String, String> buildUrls(Rental rental, PaymentType type) {
        String successSessionUrl = buildUrlWithParams(successUrl, Map.of(
                "rentalId", rental.getId(),
                "type", type
        ));
        String cancelSessionUrl = buildUrlWithParams(cancelUrl, Map.of(
                "rentalId", rental.getId()
        ));
        return Map.of(
            "successUrl", successSessionUrl,
            "cancelUrl", cancelSessionUrl
        );
    }

    private String buildUrlWithParams(String baseUrl, Map<String, Object> params) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(baseUrl);
        params.forEach(uriComponentsBuilder::queryParam);
        return uriComponentsBuilder.build().toUriString();
    }
}
