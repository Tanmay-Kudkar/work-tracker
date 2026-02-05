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
public class ActivityLogRequest {
    @NotBlank(message = "Username is required")
    private String username;

    private String applicationName;
    private String timestamp; // Optional ISO 8601 timestamp from client
    
    @Builder.Default
    private Boolean isIdle = false;
}
