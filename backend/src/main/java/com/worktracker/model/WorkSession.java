package com.worktracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_session_username", columnList = "username"),
        @Index(name = "idx_session_active", columnList = "isActive"),
        @Index(name = "idx_session_username_active", columnList = "username,isActive")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String applicationName;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;

    @Builder.Default
    private Long durationMinutes = 0L;

    @Builder.Default
    private Boolean isActive = false;
}
