package com.example.carsharing.service.car;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharing.dto.car.CarRequestDto;
import com.example.carsharing.dto.car.CarResponseDto;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.mapper.CarMapper;
import com.example.carsharing.model.Car;
import com.example.carsharing.repository.CarRepository;
import com.example.carsharing.util.CarTestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Creating a car with valid request should return CarResponseDto")
    void create_ValidRequest_ReturnsCarResponseDto() {
        CarRequestDto requestDto = CarTestDataFactory.getCarRequestDto();
        Car car = CarTestDataFactory.getCar();
        Car savedCar = CarTestDataFactory.getSavedCar();
        CarResponseDto expectedCarResponseDto = CarTestDataFactory.getCarResponseDto();

        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(savedCar);
        when(carMapper.toDto(savedCar)).thenReturn(expectedCarResponseDto);

        CarResponseDto actualCarResponseDto = carService.create(requestDto);

        assertNotNull(actualCarResponseDto);
        assertEquals(expectedCarResponseDto.id(), actualCarResponseDto.id());
        assertEquals(expectedCarResponseDto.brand(), actualCarResponseDto.brand());
        assertEquals(expectedCarResponseDto.model(), actualCarResponseDto.model());
        assertEquals(expectedCarResponseDto.type(), actualCarResponseDto.type());
        assertEquals(expectedCarResponseDto.inventory(), actualCarResponseDto.inventory());
        assertEquals(expectedCarResponseDto.dailyFee(), actualCarResponseDto.dailyFee());

        verify(carMapper).toModel(requestDto);
        verify(carRepository).save(car);
        verify(carMapper).toDto(savedCar);
    }

    @Test
    @DisplayName("Finding all cars should return paged CarResponseDto")
    void findAll_ReturnsPagedCarResponseDto() {
        CarResponseDto responseDto = CarTestDataFactory.getCarResponseDto();
        Car car = CarTestDataFactory.getSavedCar();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> carPage = new PageImpl<>(List.of(car), pageable, 1);

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(responseDto);

        Page<CarResponseDto> actualPage = carService.findAll(pageable);

        assertNotNull(actualPage);
        assertEquals(1,  actualPage.getTotalElements());
        assertEquals(responseDto, actualPage.getContent().getFirst());

        verify(carMapper).toDto(car);
        verify(carRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Finding a car by valid ID should return CarResponseDto")
    void findById_ValidId_ReturnsCarResponseDto() {
        Long carId = 1L;
        Car car = CarTestDataFactory.getSavedCar();
        CarResponseDto expectedResponseDto = CarTestDataFactory.getCarResponseDto();

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expectedResponseDto);

        CarResponseDto actualResponseDto = carService.findById(carId);

        assertNotNull(actualResponseDto);
        assertEquals(expectedResponseDto.id(), actualResponseDto.id());
        assertEquals(expectedResponseDto.brand(), actualResponseDto.brand());

        verify(carRepository).findById(carId);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Finding a car by invalid ID should throw EntityNotFoundException")
    void findById_InvalidId_ThrowsEntityNotFoundException() {
        Long invalidId = 666L;
        when(carRepository.findById(invalidId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> carService.findById(invalidId)
        );

        assertEquals("Can't find car with id: " + invalidId, exception.getMessage());
        verify(carRepository).findById(invalidId);
        verify(carMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Updating a car with valid ID and request should return updated CarResponseDto")
    void update_ValidIdAndRequest_ReturnsUpdatedCarResponseDto() {
        Long carId = 1L;
        CarRequestDto requestDto = CarTestDataFactory.getCarRequestDto();
        Car existingCar = CarTestDataFactory.getSavedCar();
        Car updatedCar = CarTestDataFactory.getSavedCar();
        CarResponseDto expectedResponseDto = CarTestDataFactory.getCarResponseDto();

        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));
        when(carRepository.save(existingCar)).thenReturn(updatedCar);
        when(carMapper.toDto(updatedCar)).thenReturn(expectedResponseDto);

        CarResponseDto actualResponseDto = carService.update(carId, requestDto);

        assertNotNull(actualResponseDto);
        assertEquals(expectedResponseDto.id(), actualResponseDto.id());

        verify(carRepository).findById(carId);
        verify(carMapper).updateCarFromDto(requestDto, existingCar);
        verify(carRepository).save(existingCar);
        verify(carMapper).toDto(updatedCar);
    }

    @Test
    @DisplayName("Updating a car with invalid ID should throw EntityNotFoundException")
    void update_InvalidId_ThrowsEntityNotFoundException() {
        Long invalidId = 666L;
        CarRequestDto requestDto = CarTestDataFactory.getCarRequestDto();

        when(carRepository.findById(invalidId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> carService.update(invalidId, requestDto)
        );

        assertEquals("Can't find car with id: " + invalidId, exception.getMessage());
        verify(carRepository).findById(invalidId);
        verify(carMapper, never()).updateCarFromDto(any(), any());
        verify(carRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deleting a car with valid ID should delete the car")
    void delete_ValidId_DeletesCar() {
        Long carId = 1L;
        when(carRepository.existsById(carId)).thenReturn(true);

        carService.delete(carId);

        verify(carRepository).existsById(carId);
        verify(carRepository).deleteById(carId);
    }

    @Test
    @DisplayName("Deleting a car with invalid ID should throw EntityNotFoundException")
    void delete_InvalidId_ThrowsEntityNotFoundException() {
        Long invalidId = 666L;
        when(carRepository.existsById(invalidId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> carService.delete(invalidId)
        );

        assertEquals("Can't delete car. ID not found: " + invalidId, exception.getMessage());
        verify(carRepository).existsById(invalidId);
        verify(carRepository, never()).deleteById(any());
    }
}
