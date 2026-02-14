package com.example.carsharing.service.overdue;

import com.example.carsharing.model.Rental;
import com.example.carsharing.repository.RentalRepository;
import com.example.carsharing.service.notification.NotificationService;
import com.example.carsharing.service.notification.factory.RentalMessageFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OverdueRentalService {

    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void checkOverdueRentals() {
        LocalDate today = LocalDate.now();
        List<Rental> overdueRentals = rentalRepository.findOverdueRentals(today);
        if (overdueRentals.isEmpty()) {
            notificationService.sendMessage("There are no overdue rentals today");
        } else {
            notificationService.sendMessage(
                    "Number of overdue rentals: " + overdueRentals.size()
                    + System.lineSeparator()
                    + "See a list below:");
            for (Rental rental : overdueRentals) {
                String message = RentalMessageFactory.rentalOverdue(rental);
                notificationService.sendMessage(message);
            }
        }
    }
}
