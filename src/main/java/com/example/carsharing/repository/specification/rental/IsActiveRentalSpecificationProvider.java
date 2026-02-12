package com.example.carsharing.repository.specification.rental;

import com.example.carsharing.model.Rental;
import com.example.carsharing.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class IsActiveRentalSpecificationProvider implements SpecificationProvider<Rental> {
    private static final String KEY = "isActive";
    private static final String FIELD_NAME = "actualReturnDate";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public Specification<Rental> getSpecification(String[] params) {
        return (root, query, cb) -> {
            boolean isActive = Boolean.parseBoolean(params[0]);
            return isActive
                    ? cb.isNull(root.get(FIELD_NAME))
                    : cb.isNotNull(root.get(FIELD_NAME));
        };
    }
}
