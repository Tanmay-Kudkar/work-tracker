package com.worktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSummaryDto {
    private String username;
    private String fullName;
    private Long totalActiveMinutes;
    private String totalActiveHours;
    private Boolean isActive;
    private String currentApplication;
    private String topApp;
}
