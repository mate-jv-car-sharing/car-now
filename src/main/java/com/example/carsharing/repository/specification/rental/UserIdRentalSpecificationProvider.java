package com.example.carsharing.repository.specification.rental;

import com.example.carsharing.model.Rental;
import com.example.carsharing.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserIdRentalSpecificationProvider implements SpecificationProvider<Rental> {
    private static final String KEY = "userId";
    private static final String FIELD_NAME = "user";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public Specification<Rental> getSpecification(String[] params) {
        return (root, query, cb)
                -> root.get(FIELD_NAME).get("id").in((Object[]) params);
    }
}
