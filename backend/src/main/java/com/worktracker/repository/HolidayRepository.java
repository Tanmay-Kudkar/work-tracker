package com.worktracker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.worktracker.model.Holiday;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> findByDateBetween(LocalDate startDate, LocalDate endDate);
    Optional<Holiday> findByDate(LocalDate date);
    List<Holiday> findByDateAfter(LocalDate date);
    boolean existsByDate(LocalDate date);
}
