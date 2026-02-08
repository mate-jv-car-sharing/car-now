package com.example.carsharing.service;


import com.example.carsharing.dto.car.CarRequestDto;
import com.example.carsharing.dto.car.CarResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto create(CarRequestDto requestDto);

    List<CarResponseDto> findAll(Pageable pageable);

    CarResponseDto findById(Long id);

    CarResponseDto update(Long id, CarRequestDto requestDto);

    void delete(Long id);
}
