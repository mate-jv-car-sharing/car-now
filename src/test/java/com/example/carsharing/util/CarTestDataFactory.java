package com.example.carsharing.util;

import com.example.carsharing.dto.car.CarRequestDto;
import com.example.carsharing.dto.car.CarResponseDto;
import com.example.carsharing.model.Car;
import com.example.carsharing.model.enums.CarType;
import java.math.BigDecimal;

public class CarTestDataFactory {

    public static Car getCar() {
        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(120));
        return car;
    }

    public static Car getSavedCar() {
        Car car = getCar();
        car.setId(1L);
        return car;
    }

    public static CarRequestDto getCarRequestDto() {
        return new CarRequestDto(
                "Toyota",
                "Camry",
                CarType.SEDAN,
                5,
                BigDecimal.valueOf(120)
        );
    }

    public static CarResponseDto getCarResponseDto() {
        return new CarResponseDto(
                1L,
                "Toyota",
                "Camry",
                CarType.SEDAN,
                5,
                BigDecimal.valueOf(120)
        );
    }
}
