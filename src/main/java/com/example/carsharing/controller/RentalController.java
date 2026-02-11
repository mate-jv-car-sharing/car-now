package com.example.carsharing.controller;

import com.example.carsharing.dto.rental.CreateRentalRequestDto;
import com.example.carsharing.dto.rental.RentalResponseDto;
import com.example.carsharing.model.User;
import com.example.carsharing.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public RentalResponseDto createRental(
            @AuthenticationPrincipal User user,
            @RequestBody CreateRentalRequestDto requestDto
    ) {
        return rentalService.create(user, requestDto);
    }

    @PostMapping("/{id}/return")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public void returnRental(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        rentalService.returnRental(user, id);
    }
}
