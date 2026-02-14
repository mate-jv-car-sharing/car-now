package com.example.carsharing.service.car;

import com.example.carsharing.dto.car.CarRequestDto;
import com.example.carsharing.dto.car.CarResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto create(CarRequestDto requestDto);

    Page<CarResponseDto> findAll(Pageable pageable);

    CarResponseDto findById(Long id);

    CarResponseDto update(Long id, CarRequestDto requestDto);

    void delete(Long id);
}
