package com.example.carsharing.repository.specification.rental;

import com.example.carsharing.dto.rental.RentalSearchParameters;
import com.example.carsharing.model.Rental;
import com.example.carsharing.repository.specification.SpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalSpecificationBuilder implements SpecificationBuilder<Rental> {
    private final RentalSpecificationProviderManager rentalSpecificationProviderManager;

    @Override
    public Specification<Rental> buildSpecification(RentalSearchParameters rentalSearchParameters) {
        Specification<Rental> spec = Specification.allOf();
        if (rentalSearchParameters.userId() != null && rentalSearchParameters.userId().length > 0) {
            spec = spec.and(rentalSpecificationProviderManager.getSpecificationProvider("userId")
                    .getSpecification(rentalSearchParameters.userId()));
        }
        if (rentalSearchParameters.isActive() != null
                && rentalSearchParameters.isActive().length > 0) {
            spec = spec.and(rentalSpecificationProviderManager.getSpecificationProvider("isActive")
                    .getSpecification(rentalSearchParameters.isActive()));
        }
        return spec;
    }
}
