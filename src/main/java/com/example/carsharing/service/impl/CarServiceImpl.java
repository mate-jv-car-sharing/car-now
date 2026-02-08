package com.example.carsharing.service.impl;

import com.example.carsharing.dto.car.CarRequestDto;
import com.example.carsharing.dto.car.CarResponseDto;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.mapper.CarMapper;
import com.example.carsharing.model.Car;
import com.example.carsharing.repository.CarRepository;
import com.example.carsharing.service.CarService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    @Transactional
    public CarResponseDto create(CarRequestDto requestDto) {
        Car newCar = carMapper.toModel(requestDto);
        return carMapper.toDto(carRepository.save(newCar));
    }

    @Override
    public List<CarResponseDto> findAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .stream()
                .map(carMapper::toDto)
                .toList();
    }

    @Override
    public CarResponseDto findById(Long id) {
        Car existingCar = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id: " + id)
        );
        return carMapper.toDto(existingCar);
    }

    @Override
    @Transactional
    public CarResponseDto update(Long id, CarRequestDto requestDto) {
        Car existingCar = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id: " + id)
        );
        carMapper.updateCarFromDto(requestDto, existingCar);
        return carMapper.toDto(carRepository.save(existingCar));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't delete car. ID not found: " + id);
        }
        carRepository.deleteById(id);
    }
}
