package com.example.carsharing.service.overdue;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.carsharing.model.Rental;
import com.example.carsharing.repository.RentalRepository;
import com.example.carsharing.service.notification.NotificationService;
import com.example.carsharing.service.notification.factory.RentalMessageFactory;
import com.example.carsharing.util.RentalTestDataFactory;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OverdueRentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OverdueRentalService overdueRentalService;

    @Test
    @DisplayName("Sends 'no overdue rentals' message when repository returns empty list")
    void checkOverdueRentals_whenNoOverdueRentals_sendsNoOverdueMessage() {
        when(rentalRepository.findOverdueRentals(any(LocalDate.class)))
                .thenReturn(List.of());

        overdueRentalService.checkOverdueRentals();

        verify(rentalRepository).findOverdueRentals(any(LocalDate.class));
        verify(notificationService).sendMessage("There are no overdue rentals today");
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Sends summary + one message per overdue rental when repository returns rentals")
    void checkOverdueRentals_whenOverdueRentalsExist_sendsSummaryAndEachRentalMessage() {
        Rental overdueRentalOne = RentalTestDataFactory.getOverdueRental();
        Rental overdueRentalTwo = RentalTestDataFactory.getOverdueRental();
        ReflectionTestUtils.setField(overdueRentalTwo, "id", 2L);
        when(rentalRepository.findOverdueRentals(any(LocalDate.class)))
                .thenReturn(List.of(overdueRentalOne, overdueRentalTwo));

        overdueRentalService.checkOverdueRentals();

        verify(rentalRepository).findOverdueRentals(any(LocalDate.class));

        String expectedHeader = "Number of overdue rentals: 2"
                + System.lineSeparator()
                + "See a list below:";

        String msg1 = RentalMessageFactory.rentalOverdue(overdueRentalOne);
        String msg2 = RentalMessageFactory.rentalOverdue(overdueRentalTwo);

        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).sendMessage(expectedHeader);
        inOrder.verify(notificationService).sendMessage(msg1);
        inOrder.verify(notificationService).sendMessage(msg2);

        verifyNoMoreInteractions(notificationService);
    }
}
