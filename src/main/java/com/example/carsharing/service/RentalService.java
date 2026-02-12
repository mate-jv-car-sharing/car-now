package com.example.carsharing.service;

import com.example.carsharing.dto.rental.CreateRentalRequestDto;
import com.example.carsharing.dto.rental.RentalResponseDto;
import com.example.carsharing.dto.rental.RentalSearchParameters;
import com.example.carsharing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalResponseDto create(User user, CreateRentalRequestDto requestDto);

    RentalResponseDto findById(User user, Long id);

    Page<RentalResponseDto> getRentals(
            User user,
            RentalSearchParameters rentalSearchParameters,
            Pageable pageable
    );

    void returnRental(User user, Long rentalId);
}
