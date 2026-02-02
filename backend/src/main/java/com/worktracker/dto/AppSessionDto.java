package com.worktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSessionDto {
    private Long id;
    private String applicationName;
    private String processName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isActive;
    private String endReason;
    private Long durationSeconds;
    private String formattedDuration;
    private String formattedStartTime;
    private String formattedEndTime;
}
