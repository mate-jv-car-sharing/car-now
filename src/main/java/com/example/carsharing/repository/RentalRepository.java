package com.example.carsharing.repository;

import com.example.carsharing.model.Rental;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long>,
        JpaSpecificationExecutor<Rental> {

    @EntityGraph(attributePaths = {"user", "car"})
    @Query("SELECT r FROM Rental r WHERE r.returnDate <= :today AND r.actualReturnDate IS NULL")
    List<Rental> findOverdueRentals(@Param("today") LocalDate today);
}
