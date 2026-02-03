package com.worktracker.controller;

import com.worktracker.dto.ActivityLogRequest;
import com.worktracker.dto.ApiResponse;
import com.worktracker.dto.MemberSummaryDto;
import com.worktracker.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> logActivity(
            @Valid @RequestBody ActivityLogRequest request) {
        activityService.logActivity(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "ok")));
    }

    @GetMapping("/dashboard/{username}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @PathVariable String username,
            @RequestParam(required = false) String date,
            @RequestParam(required = false, defaultValue = "0") int tzOffsetMinutes) {
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        Map<String, Object> dashboard = activityService.getDashboard(username, targetDate,
                clampTzOffsetMinutes(tzOffsetMinutes));
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<MemberSummaryDto>>> getAllMembersSummary(
            @RequestParam(required = false) String date,
            @RequestParam(required = false, defaultValue = "0") int tzOffsetMinutes) {
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        List<MemberSummaryDto> summaries = activityService.getAllMembersSummary(targetDate,
                clampTzOffsetMinutes(tzOffsetMinutes));
        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWeeklySummary(
            @RequestParam(required = false) String date,
            @RequestParam(required = false, defaultValue = "0") int tzOffsetMinutes) {
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        Map<String, Object> weeklySummary = activityService.getWeeklySummary(targetDate,
                clampTzOffsetMinutes(tzOffsetMinutes));
        return ResponseEntity.ok(ApiResponse.success(weeklySummary));
    }

    private static int clampTzOffsetMinutes(int tzOffsetMinutes) {
        // Keep in a sane range: UTC-14 to UTC+14
        int min = -14 * 60;
        int max = 14 * 60;
        return Math.max(min, Math.min(max, tzOffsetMinutes));
    }
}
