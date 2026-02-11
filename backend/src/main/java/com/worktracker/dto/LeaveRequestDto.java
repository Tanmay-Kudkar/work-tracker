package com.worktracker.dto;

import java.time.LocalDate;

import com.worktracker.model.LeaveRequest.LeaveType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDto {
    private String username;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private String reason;
}
