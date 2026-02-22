package com.example.carsharing.util;

import com.example.carsharing.model.Rental;
import java.time.LocalDate;
import org.springframework.test.util.ReflectionTestUtils;

public class RentalTestDataFactory {
    private static final Long DEFAULT_ID = 1L;
    private static final LocalDate DEFAULT_RENTAL_DATE = LocalDate.of(2026, 2, 10);
    private static final LocalDate DEFAULT_RETURN_DATE = LocalDate.of(2026, 2, 15);

    public static Rental getRental() {
        Rental rental = new Rental();
        rental.setRentalDate(DEFAULT_RENTAL_DATE);
        rental.setReturnDate(DEFAULT_RETURN_DATE);
        rental.setCar(CarTestDataFactory.getSavedCar());
        rental.setUser(UserTestDataFactory.getSavedUser());
        return rental;
    }

    public static Rental getSavedRental() {
        Rental rental = getRental();
        ReflectionTestUtils.setField(rental, "id", DEFAULT_ID);
        return rental;
    }

    public static Rental getActiveRental() {
        return getSavedRental();
    }

    public static Rental getReturnedOnTimeRental() {
        Rental rental = getSavedRental();
        rental.setActualReturnDate(DEFAULT_RETURN_DATE);
        return rental;
    }

    public static Rental getOverdueRental() {
        Rental rental = getSavedRental();
        rental.setActualReturnDate(DEFAULT_RETURN_DATE.plusDays(2));
        return rental;
    }
}
