package com.worktracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.worktracker.model.LeaveRequest;
import com.worktracker.model.LeaveRequest.LeaveStatus;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUsername(String username);
    
    List<LeaveRequest> findByUsernameAndStatus(String username, LeaveStatus status);
    
    List<LeaveRequest> findByStatus(LeaveStatus status);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.username = :username " +
           "AND lr.status = :status " +
           "AND ((lr.startDate <= :endDate) AND (lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaves(
        @Param("username") String username,
        @Param("status") LeaveStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "((lr.startDate <= :endDate) AND (lr.endDate >= :startDate)) " +
           "AND lr.status = 'APPROVED'")
    List<LeaveRequest> findApprovedLeavesBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.username = :username " +
           "AND lr.startDate <= :date AND lr.endDate >= :date " +
           "AND lr.status = 'APPROVED'")
    List<LeaveRequest> findApprovedLeaveForUserOnDate(
        @Param("username") String username,
        @Param("date") LocalDate date
    );
}
