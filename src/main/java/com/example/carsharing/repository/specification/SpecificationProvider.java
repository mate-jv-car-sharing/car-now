package com.example.carsharing.repository.specification;

import com.example.carsharing.model.Rental;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationProvider<T> {
    String getKey();

    Specification<Rental> getSpecification(String[] params);
}
