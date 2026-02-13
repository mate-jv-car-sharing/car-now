package com.example.carsharing.service.notification.factory;

import com.example.carsharing.model.Rental;
import java.time.format.DateTimeFormatter;

public class RentalMessageFactory {

    public static String rentalCreation(Rental rental) {
        return String.format("""
        🚗 New rental created:
        - User: %s
        - Brand: %s
        - Model: %s
        - Available cars: %d
        - Return date: %s
        """,
        rental.getUser().getEmail(),
        rental.getCar().getBrand(),
        rental.getCar().getModel(),
        rental.getCar().getInventory(),
        rental.getReturnDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    public static String rentalReturning(Rental rental) {
        return String.format("""
        🚗 Rental returned recently:
        - User: %s
        - Brand: %s
        - Model: %s
        - Available cars: %d
        """,
        rental.getUser().getEmail(),
        rental.getCar().getBrand(),
        rental.getCar().getModel(),
        rental.getCar().getInventory());
    }
}
