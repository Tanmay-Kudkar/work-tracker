package com.worktracker.repository;

import com.worktracker.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUsernameOrderByTimestampDesc(String username);

    List<ActivityLog> findAllByOrderByTimestampDesc();

    List<ActivityLog> findByUsernameAndTimestampBetweenOrderByTimestampAsc(
            String username, LocalDateTime start, LocalDateTime end);

    List<ActivityLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start, LocalDateTime end);
}
