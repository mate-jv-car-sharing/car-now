package com.example.carsharing.controller;

import com.example.carsharing.dto.rental.CreateRentalRequestDto;
import com.example.carsharing.dto.rental.RentalResponseDto;
import com.example.carsharing.dto.rental.RentalSearchParameters;
import com.example.carsharing.model.User;
import com.example.carsharing.service.rental.RentalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RentalResponseDto createRental(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateRentalRequestDto requestDto
    ) {
        return rentalService.create(user, requestDto);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @GetMapping("/{id}")
    public RentalResponseDto findById(
            @AuthenticationPrincipal User user,
            @Positive @PathVariable Long id
    ) {
        return rentalService.findById(user, id);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @GetMapping
    public Page<RentalResponseDto> getRentals(
            @AuthenticationPrincipal User user,
            RentalSearchParameters searchParameters,
            @PageableDefault(size = 15)Pageable pageable
    ) {
        return rentalService.getRentals(user, searchParameters, pageable);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{id}/return")
    public void returnRental(
            @AuthenticationPrincipal User user,
            @Positive @PathVariable Long id
    ) {
        rentalService.returnRental(user, id);
    }
}
