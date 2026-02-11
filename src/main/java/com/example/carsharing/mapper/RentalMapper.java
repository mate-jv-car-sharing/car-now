package com.example.carsharing.mapper;

import com.example.carsharing.config.MapperConfig;
import com.example.carsharing.dto.rental.CreateRentalRequestDto;
import com.example.carsharing.dto.rental.RentalResponseDto;
import com.example.carsharing.model.Car;
import com.example.carsharing.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "user.id", target = "userId")
    RentalResponseDto toDto(Rental rental);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rentalDate", ignore = true)
    @Mapping(target = "actualReturnDate", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(source = "carId", target = "car")
    Rental toModel(CreateRentalRequestDto dto);

    default Car mapCar(Long carId) {
        Car car = new Car();
        car.setId(carId);
        return car;
    }
}
