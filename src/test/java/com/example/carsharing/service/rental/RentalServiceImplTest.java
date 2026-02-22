package com.example.carsharing.service.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.carsharing.dto.rental.CreateRentalRequestDto;
import com.example.carsharing.dto.rental.RentalResponseDto;
import com.example.carsharing.dto.rental.RentalSearchParameters;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.exception.RentalException;
import com.example.carsharing.model.Car;
import com.example.carsharing.model.Role;
import com.example.carsharing.model.User;
import com.example.carsharing.repository.CarRepository;
import com.example.carsharing.repository.RentalRepository;
import com.example.carsharing.repository.RoleRepository;
import com.example.carsharing.repository.UserRepository;
import com.example.carsharing.service.notification.NotificationService;
import com.example.carsharing.util.CarTestDataFactory;
import com.example.carsharing.util.UserTestDataFactory;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Sql(scripts = {
        "classpath:database/delete-all-rentals.sql",
        "classpath:database/delete-all-cars.sql",
        "classpath:database/delete-all-users.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RentalServiceImplTest {

    private final RentalService rentalService;
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @MockBean
    private NotificationService notificationService;

    private User customer;
    private User manager;
    private Car car;

    @Autowired
    public RentalServiceImplTest(RentalService rentalService,
                                 RentalRepository rentalRepository,
                                 CarRepository carRepository,
                                 UserRepository userRepository,
                                 RoleRepository roleRepository) {
        this.rentalService = rentalService;
        this.rentalRepository = rentalRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @BeforeEach
    void setUp() {
        Role customerRole = roleRepository.save(UserTestDataFactory.getCustomerRole());
        Role managerRole = roleRepository.save(UserTestDataFactory.getManagerRole());

        customer = userRepository.save(UserTestDataFactory.getCustomerUserWithoutId(customerRole));
        manager = userRepository.save(UserTestDataFactory.getManagerUserWithoutId(managerRole));
        car = carRepository.save(CarTestDataFactory.getCar());
    }

    @Test
    void create_ValidRequest_ReturnsRentalResponseDtoAndDecreasesInventory() {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                LocalDate.now().plusDays(5),
                car.getId()
        );
        int initialInventory = car.getInventory();

        RentalResponseDto responseDto = rentalService.create(customer, requestDto);

        assertNotNull(responseDto);
        assertEquals(requestDto.carId(), responseDto.carId());
        assertEquals(requestDto.returnDate(), responseDto.returnDate());
        assertEquals(customer.getId(), responseDto.userId());

        Car updatedCar = carRepository.findById(car.getId()).orElseThrow();
        assertEquals(initialInventory - 1, updatedCar.getInventory());
    }

    @Test
    void create_NotExistingId_ThrowsEntityNotFoundException() {
        Long nonExistingCarId = 666L;
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                LocalDate.now().plusDays(5),
                nonExistingCarId
        );
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.create(customer, requestDto)
        );
        assertEquals(
                "Can't find car with id " + requestDto.carId(),
                exception.getMessage()
        );
    }

    @Test
    void create_NoInventory_ThrowsRentalException() {
        car.setInventory(0);
        carRepository.saveAndFlush(car);

        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                LocalDate.now().plusDays(7),
                car.getId()
        );

        RentalException exception = assertThrows(RentalException.class,
                () -> rentalService.create(customer, requestDto));
        assertTrue(exception.getMessage().contains("No cars"));
    }

    @Test
    void getRentals_AsCustomer_ReturnsOnlyOwnRentals() {
        CreateRentalRequestDto requestDto1 = new CreateRentalRequestDto(
                LocalDate.now().plusDays(7),
                car.getId()
        );
        rentalService.create(customer, requestDto1);

        CreateRentalRequestDto requestDto2 = new CreateRentalRequestDto(
                LocalDate.now().plusDays(10),
                car.getId()
        );
        rentalService.create(manager, requestDto2);

        RentalSearchParameters searchParams = new RentalSearchParameters(null, null);

        Page<RentalResponseDto> customerRentals = rentalService.getRentals(
                customer, searchParams, PageRequest.of(0, 10));

        assertEquals(1, customerRentals.getTotalElements());
        assertEquals(customer.getId(), customerRentals.getContent().getFirst().userId());
    }

    @Test
    void getRentals_AsManager_ReturnsAllRentals() {
        CreateRentalRequestDto requestDto1 = new CreateRentalRequestDto(
                LocalDate.now().plusDays(7),
                car.getId()
        );
        rentalService.create(customer, requestDto1);

        CreateRentalRequestDto requestDto2 = new CreateRentalRequestDto(
                LocalDate.now().plusDays(10),
                car.getId()
        );
        rentalService.create(manager, requestDto2);

        RentalSearchParameters searchParams = new RentalSearchParameters(null, null);

        Page<RentalResponseDto> allRentals = rentalService.getRentals(
                manager, searchParams, PageRequest.of(0, 10));

        Set<Long> userIds = allRentals.getContent()
                .stream()
                .map(RentalResponseDto::userId)
                .collect(Collectors.toSet());

        assertEquals(2, allRentals.getTotalElements());
        assertTrue(userIds.contains(customer.getId()));
        assertTrue(userIds.contains(manager.getId()));
    }

    @Test
    void returnRental_ValidRental_IncreasesInventoryAndSetsReturnDate() {

        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                LocalDate.now().plusDays(7),
                car.getId()
        );
        RentalResponseDto rental = rentalService.create(customer, requestDto);
        int inventoryAfterRental = carRepository.findById(car.getId())
                .orElseThrow().getInventory();

        rentalService.returnRental(customer, rental.id());

        Car updatedCar = carRepository.findById(car.getId()).orElseThrow();
        RentalResponseDto returnedRental = rentalService.findById(customer, rental.id());

        assertNotNull(returnedRental.actualReturnDate());
        assertEquals(inventoryAfterRental + 1, updatedCar.getInventory());
    }

    @Test
    void returnRental_AlreadyReturned_ThrowsRentalException() {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                LocalDate.now().plusDays(7),
                car.getId()
        );
        RentalResponseDto rental = rentalService.create(customer, requestDto);
        rentalService.returnRental(customer, rental.id());


        assertThrows(RentalException.class,
                () -> rentalService.returnRental(customer, rental.id()));
    }

    @Test
    void getRentals_FilterByActiveStatus_ReturnsOnlyActiveRentals() {

        CreateRentalRequestDto requestDto1 = new CreateRentalRequestDto(
                LocalDate.now().plusDays(7),
                car.getId()
        );
        RentalResponseDto rental1 = rentalService.create(customer, requestDto1);

        CreateRentalRequestDto requestDto2 = new CreateRentalRequestDto(
                LocalDate.now().plusDays(10),
                car.getId()
        );
        rentalService.create(customer, requestDto2);

        rentalService.returnRental(customer, rental1.id());

        RentalSearchParameters activeOnly = new RentalSearchParameters(
                null,
                new String[]{"true"}
        );

        Page<RentalResponseDto> activeRentals = rentalService.getRentals(
                customer, activeOnly, PageRequest.of(0, 10));

        assertEquals(1, activeRentals.getTotalElements());
        assertNull(activeRentals.getContent().getFirst().actualReturnDate());
    }
}
