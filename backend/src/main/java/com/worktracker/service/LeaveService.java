package com.worktracker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worktracker.exception.ResourceNotFoundException;
import com.worktracker.model.Holiday;
import com.worktracker.model.LeaveRequest;
import com.worktracker.model.LeaveRequest.LeaveStatus;
import com.worktracker.repository.HolidayRepository;
import com.worktracker.repository.LeaveRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveService {
    
    private final LeaveRequestRepository leaveRequestRepository;
    private final HolidayRepository holidayRepository;
    
    // ========== Leave Request Operations ==========
    
    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        // Validate dates
        if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        
        // Check for overlapping approved leaves
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
            leaveRequest.getUsername(),
            LeaveStatus.APPROVED,
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate()
        );
        
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Leave request overlaps with existing approved leave");
        }
        
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setRequestedAt(LocalDateTime.now());
        return leaveRequestRepository.save(leaveRequest);
    }
    
    @Transactional
    public LeaveRequest approveLeave(Long leaveId, String approvedBy) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approvedBy);
        leave.setApprovedAt(LocalDateTime.now());
        return leaveRequestRepository.save(leave);
    }
    
    @Transactional
    public LeaveRequest rejectLeave(Long leaveId, String rejectedBy) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(rejectedBy);
        leave.setApprovedAt(LocalDateTime.now());
        return leaveRequestRepository.save(leave);
    }
    
    public List<LeaveRequest> getLeavesByUsername(String username) {
        return leaveRequestRepository.findByUsername(username);
    }
    
    public List<LeaveRequest> getPendingLeaves() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
    }
    
    public List<LeaveRequest> getApprovedLeavesBetween(LocalDate startDate, LocalDate endDate) {
        return leaveRequestRepository.findApprovedLeavesBetween(startDate, endDate);
    }
    
    public boolean isUserOnLeave(String username, LocalDate date) {
        List<LeaveRequest> leaves = leaveRequestRepository.findApprovedLeaveForUserOnDate(username, date);
        return !leaves.isEmpty();
    }
    
    @Transactional
    public void deleteLeaveRequest(Long leaveId) {
        if (!leaveRequestRepository.existsById(leaveId)) {
            throw new ResourceNotFoundException("Leave request not found");
        }
        leaveRequestRepository.deleteById(leaveId);
    }
    
    // ========== Holiday Operations ==========
    
    @Transactional
    public Holiday createHoliday(Holiday holiday) {
        if (holidayRepository.existsByDate(holiday.getDate())) {
            throw new IllegalArgumentException("Holiday already exists for this date");
        }
        holiday.setCreatedAt(LocalDateTime.now());
        return holidayRepository.save(holiday);
    }
    
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }
    
    public List<Holiday> getHolidaysBetween(LocalDate startDate, LocalDate endDate) {
        return holidayRepository.findByDateBetween(startDate, endDate);
    }
    
    public List<Holiday> getUpcomingHolidays() {
        return holidayRepository.findByDateAfter(LocalDate.now());
    }
    
    public boolean isHoliday(LocalDate date) {
        return holidayRepository.existsByDate(date);
    }
    
    @Transactional
    public void deleteHoliday(Long holidayId) {
        if (!holidayRepository.existsById(holidayId)) {
            throw new ResourceNotFoundException("Holiday not found");
        }
        holidayRepository.deleteById(holidayId);
    }
    
    @Transactional
    public Holiday updateHoliday(Long holidayId, Holiday updatedHoliday) {
        Holiday holiday = holidayRepository.findById(holidayId)
            .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));
        
        holiday.setName(updatedHoliday.getName());
        holiday.setDate(updatedHoliday.getDate());
        holiday.setDescription(updatedHoliday.getDescription());
        return holidayRepository.save(holiday);
    }
}
