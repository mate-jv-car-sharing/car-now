package com.example.carsharing.repository.specification;

import com.example.carsharing.dto.rental.RentalSearchParameters;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {
    Specification<T> buildSpecification(RentalSearchParameters rentalSearchParameters);
}
