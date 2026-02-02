package com.worktracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEventRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Application name is required")
    private String applicationName;

    private String processName;

    @NotBlank(message = "Event type is required")
    private String eventType; // "start", "end", "heartbeat"

    private String endReason; // "normal", "killed", "timeout"
}
