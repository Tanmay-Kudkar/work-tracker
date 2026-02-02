package com.worktracker.repository;

import com.worktracker.model.AppSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppSessionRepository extends JpaRepository<AppSession, Long> {

    List<AppSession> findByUsernameAndStartTimeBetweenOrderByStartTimeDesc(
            String username, LocalDateTime start, LocalDateTime end);

    List<AppSession> findByUsernameAndIsActiveTrueOrderByStartTimeDesc(String username);

    Optional<AppSession> findByUsernameAndApplicationNameAndIsActiveTrue(
            String username, String applicationName);

    @Query("SELECT a FROM AppSession a WHERE a.username = :username AND a.startTime >= :start ORDER BY a.startTime DESC")
    List<AppSession> findRecentSessions(@Param("username") String username, @Param("start") LocalDateTime start);

    @Modifying
    @Transactional
    @Query("UPDATE AppSession a SET a.isActive = false, a.endTime = :endTime, a.endReason = :reason, " +
            "a.durationSeconds = CAST(TIMESTAMPDIFF(SECOND, a.startTime, :endTime) AS long) " +
            "WHERE a.username = :username AND a.isActive = true AND a.startTime < :threshold")
    int closeTimedOutSessions(@Param("username") String username,
            @Param("endTime") LocalDateTime endTime,
            @Param("reason") String reason,
            @Param("threshold") LocalDateTime threshold);
}
