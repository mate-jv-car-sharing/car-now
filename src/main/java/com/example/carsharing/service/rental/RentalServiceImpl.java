package com.example.carsharing.service.rental;

import com.example.carsharing.dto.rental.CreateRentalRequestDto;
import com.example.carsharing.dto.rental.RentalResponseDto;
import com.example.carsharing.dto.rental.RentalSearchParameters;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.exception.RentalException;
import com.example.carsharing.mapper.RentalMapper;
import com.example.carsharing.model.Car;
import com.example.carsharing.model.Rental;
import com.example.carsharing.model.Role;
import com.example.carsharing.model.User;
import com.example.carsharing.repository.CarRepository;
import com.example.carsharing.repository.RentalRepository;
import com.example.carsharing.repository.specification.rental.RentalSpecificationBuilder;
import com.example.carsharing.service.notification.NotificationService;
import com.example.carsharing.service.notification.factory.RentalMessageFactory;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;
    private final RentalSpecificationBuilder rentalSpecificationBuilder;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RentalResponseDto create(User user, CreateRentalRequestDto requestDto) {
        Car existingCar = carRepository.findById(requestDto.carId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id " + requestDto.carId())
        );
        if (existingCar.getInventory() <= 0) {
            throw new RentalException("No cars with id " + requestDto.carId() + " available");
        }

        existingCar.setInventory(existingCar.getInventory() - 1);

        Rental rental = rentalMapper.toModel(requestDto);
        rental.setCar(existingCar);
        rental.setUser(user);

        notificationService.sendMessage(RentalMessageFactory.rentalCreation(rental));

        return rentalMapper.toDto(rentalRepository.save(rental));
    }

    @Override
    public RentalResponseDto findById(User user, Long id) {
        Rental rental = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id " + id)
        );
        validateRentalOwnership(user, rental);
        return rentalMapper.toDto(rental);
    }

    @Override
    public Page<RentalResponseDto> getRentals(
            User user,
            RentalSearchParameters rentalSearchParameters,
            Pageable pageable
    ) {
        boolean isManager = isManager(user);
        RentalSearchParameters securedParams =
                secureSearchParams(user, rentalSearchParameters, isManager);
        Specification<Rental> rentalSpecification =
                rentalSpecificationBuilder.buildSpecification(securedParams);
        return rentalRepository.findAll(rentalSpecification, pageable)
                .map(rentalMapper::toDto);
    }

    @Override
    @Transactional
    public void returnRental(User user, Long rentalId) {
        Rental existingRental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id " + rentalId)
        );
        validateRentalOwnership(user, existingRental);
        if (existingRental.getActualReturnDate() != null) {
            throw new RentalException("Rental with id " + rentalId + " is already returned");
        }
        existingRental.setActualReturnDate(LocalDate.now());
        Car rentedCar = existingRental.getCar();
        rentedCar.setInventory(rentedCar.getInventory() + 1);
        notificationService.sendMessage(RentalMessageFactory.rentalReturning(existingRental));
        rentalRepository.save(existingRental);
    }

    private static boolean isManager(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.MANAGER);
    }

    private static void validateRentalOwnership(User user, Rental rental) {
        boolean isManager = isManager(user);
        if (!isManager && !rental.getUser().getId().equals(user.getId())) {
            throw new RentalException(String.format(
                    "User with id %d is not the owner of the rental with id %d",
                    user.getId(),
                    rental.getId()
            ));
        }
    }

    private static RentalSearchParameters secureSearchParams(
            User user,
            RentalSearchParameters rentalSearchParameters,
            boolean isManager) {
        if (isManager) {
            return rentalSearchParameters;
        }
        String[] isActive = rentalSearchParameters == null
                ? null
                : rentalSearchParameters.isActive();
        return new RentalSearchParameters(
                new String[]{user.getId().toString()},
                isActive
        );
    }
}
