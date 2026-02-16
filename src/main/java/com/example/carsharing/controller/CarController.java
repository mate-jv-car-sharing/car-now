package com.example.carsharing.controller;

import com.example.carsharing.dto.car.CarRequestDto;
import com.example.carsharing.dto.car.CarResponseDto;
import com.example.carsharing.service.car.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
@Tag(name = "Cars", description = "Car management APIs")
public class CarController {
    private final CarService carService;

    @Operation(
            summary = "Create a new car",
            description = "Add a new car to the inventory. Manager only."
    )
    @PreAuthorize("hasAnyRole('MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponseDto create(@Valid @RequestBody CarRequestDto requestDto) {
        return carService.create(requestDto);
    }

    @Operation(
            summary = "Get all cars",
            description = "Retrieve a paginated list of all available cars"
    )
    @GetMapping
    public Page<CarResponseDto> findAll(
            @PageableDefault(size = 15, sort = "id") Pageable pageable
    ) {
        return carService.findAll(pageable);
    }

    @Operation(summary = "Get car by ID")
    @GetMapping("/{id}")
    public CarResponseDto findCarById(@Positive @PathVariable Long id) {
        return carService.findById(id);
    }

    @Operation(summary = "Update car", description = "Update car details. Manager only.")
    @PreAuthorize("hasAnyRole('MANAGER')")
    @PutMapping("/{id}")
    public CarResponseDto update(@Positive @PathVariable Long id,
                                 @Valid @RequestBody CarRequestDto requestDto) {
        return carService.update(id, requestDto);
    }

    @Operation(summary = "Delete car", description = "Remove car from inventory. Manager only.")
    @PreAuthorize("hasAnyRole('MANAGER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long id) {
        carService.delete(id);
    }
}
