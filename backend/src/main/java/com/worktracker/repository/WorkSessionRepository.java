package com.worktracker.repository;

import com.worktracker.model.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {
    List<WorkSession> findByUsernameOrderByLoginTimeDesc(String username);

    List<WorkSession> findAllByOrderByLoginTimeDesc();

    Optional<WorkSession> findByUsernameAndIsActiveTrue(String username);

    List<WorkSession> findByIsActiveTrue();

    @Query("SELECT SUM(w.durationMinutes) FROM WorkSession w WHERE w.username = :username")
    Long getTotalMinutesByUsername(String username);
}
