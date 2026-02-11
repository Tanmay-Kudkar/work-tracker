package com.worktracker.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.worktracker.dto.ApiResponse;
import com.worktracker.dto.HolidayDto;
import com.worktracker.dto.LeaveRequestDto;
import com.worktracker.model.Holiday;
import com.worktracker.model.LeaveRequest;
import com.worktracker.service.LeaveService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeaveController {
    
    private final LeaveService leaveService;
    
    // ========== Leave Request Endpoints ==========
    
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<LeaveRequest>> createLeaveRequest(@RequestBody LeaveRequestDto dto) {
        try {
            LeaveRequest leave = LeaveRequest.builder()
                .username(dto.getUsername())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .leaveType(dto.getLeaveType())
                .reason(dto.getReason())
                .build();
            
            LeaveRequest created = leaveService.createLeaveRequest(leave);
            return ResponseEntity.ok(ApiResponse.success(created, "Leave request created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getUserLeaves(@PathVariable String username) {
        List<LeaveRequest> leaves = leaveService.getLeavesByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(leaves, "Leaves retrieved successfully"));
    }
    
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getPendingLeaves() {
        List<LeaveRequest> leaves = leaveService.getPendingLeaves();
        return ResponseEntity.ok(ApiResponse.success(leaves, "Pending leaves retrieved successfully"));
    }
    
    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getApprovedLeaves(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<LeaveRequest> leaves = leaveService.getApprovedLeavesBetween(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(leaves, "Approved leaves retrieved successfully"));
    }
    
    @PostMapping("/{leaveId}/approve")
    public ResponseEntity<ApiResponse<LeaveRequest>> approveLeave(
            @PathVariable Long leaveId,
            @RequestParam String approvedBy) {
        try {
            LeaveRequest approved = leaveService.approveLeave(leaveId, approvedBy);
            return ResponseEntity.ok(ApiResponse.success(approved, "Leave approved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/{leaveId}/reject")
    public ResponseEntity<ApiResponse<LeaveRequest>> rejectLeave(
            @PathVariable Long leaveId,
            @RequestParam String rejectedBy) {
        try {
            LeaveRequest rejected = leaveService.rejectLeave(leaveId, rejectedBy);
            return ResponseEntity.ok(ApiResponse.success(rejected, "Leave rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{leaveId}")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveRequest(@PathVariable Long leaveId) {
        try {
            leaveService.deleteLeaveRequest(leaveId);
            return ResponseEntity.ok(ApiResponse.success(null, "Leave request deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkUserOnLeave(
            @RequestParam String username,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean onLeave = leaveService.isUserOnLeave(username, date);
        return ResponseEntity.ok(ApiResponse.success(onLeave, "Leave status checked successfully"));
    }
    
    // ========== Holiday Endpoints ==========
    
    @PostMapping("/holidays")
    public ResponseEntity<ApiResponse<Holiday>> createHoliday(@RequestBody HolidayDto dto) {
        try {
            Holiday holiday = Holiday.builder()
                .name(dto.getName())
                .date(dto.getDate())
                .description(dto.getDescription())
                .build();
            
            Holiday created = leaveService.createHoliday(holiday);
            return ResponseEntity.ok(ApiResponse.success(created, "Holiday created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/holidays")
    public ResponseEntity<ApiResponse<List<Holiday>>> getAllHolidays() {
        List<Holiday> holidays = leaveService.getAllHolidays();
        return ResponseEntity.ok(ApiResponse.success(holidays, "Holidays retrieved successfully"));
    }
    
    @GetMapping("/holidays/upcoming")
    public ResponseEntity<ApiResponse<List<Holiday>>> getUpcomingHolidays() {
        List<Holiday> holidays = leaveService.getUpcomingHolidays();
        return ResponseEntity.ok(ApiResponse.success(holidays, "Upcoming holidays retrieved successfully"));
    }
    
    @GetMapping("/holidays/range")
    public ResponseEntity<ApiResponse<List<Holiday>>> getHolidaysInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Holiday> holidays = leaveService.getHolidaysBetween(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(holidays, "Holidays retrieved successfully"));
    }
    
    @DeleteMapping("/holidays/{holidayId}")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(@PathVariable Long holidayId) {
        try {
            leaveService.deleteHoliday(holidayId);
            return ResponseEntity.ok(ApiResponse.success(null, "Holiday deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/holidays/{holidayId}")
    public ResponseEntity<ApiResponse<Holiday>> updateHoliday(
            @PathVariable Long holidayId,
            @RequestBody HolidayDto dto) {
        try {
            Holiday holiday = Holiday.builder()
                .name(dto.getName())
                .date(dto.getDate())
                .description(dto.getDescription())
                .build();
            
            Holiday updated = leaveService.updateHoliday(holidayId, holiday);
            return ResponseEntity.ok(ApiResponse.success(updated, "Holiday updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
