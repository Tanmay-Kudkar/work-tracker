package com.worktracker.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(indexes = {
        @Index(name = "idx_session_username", columnList = "username"),
        @Index(name = "idx_session_start", columnList = "startTime"),
        @Index(name = "idx_session_username_start", columnList = "username,startTime")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String applicationName;
    private String processName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean isActive;
    private String endReason; // "normal", "killed", "timeout", "system_shutdown"

    // Duration in seconds (calculated when session ends)
    private Long durationSeconds;
}
