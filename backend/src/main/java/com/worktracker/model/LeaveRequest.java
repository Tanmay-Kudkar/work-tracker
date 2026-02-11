package com.worktracker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(indexes = {
        @Index(name = "idx_leave_username", columnList = "username"),
        @Index(name = "idx_leave_dates", columnList = "startDate,endDate"),
        @Index(name = "idx_leave_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();

    private String approvedBy;

    private LocalDateTime approvedAt;

    public enum LeaveType {
        SICK,
        VACATION,
        PERSONAL,
        EMERGENCY,
        OTHER
    }

    public enum LeaveStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
