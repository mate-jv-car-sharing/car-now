package com.example.carsharing.service;

import com.example.carsharing.dto.rental.CreateRentalRequestDto;
import com.example.carsharing.dto.rental.RentalResponseDto;
import com.example.carsharing.model.User;

public interface RentalService {
    RentalResponseDto create(User user, CreateRentalRequestDto requestDto);

    RentalResponseDto findById(User user, Long id);

    void returnRental(User user, Long rentalId);
}
