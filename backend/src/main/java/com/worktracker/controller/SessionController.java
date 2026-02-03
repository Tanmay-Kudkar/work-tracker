package com.worktracker.controller;

import com.worktracker.dto.ApiResponse;
import com.worktracker.dto.SessionEventRequest;
import com.worktracker.model.WorkSession;
import com.worktracker.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // Session Event Tracking (for tracker.py)
    @PostMapping
    public ApiResponse<Map<String, String>> handleSessionEvent(@RequestBody SessionEventRequest request) {
        return sessionService.processSessionEvent(request);
    }

    // Work Session Tracking
    @PostMapping("/heartbeat")
    public ApiResponse<Map<String, String>> heartbeat(@RequestBody Map<String, String> payload) {
        return sessionService.processHeartbeat(
                payload.get("username"),
                payload.get("applicationName"));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, String>> logout(@RequestBody Map<String, String> payload) {
        return sessionService.processLogout(payload.get("username"));
    }

    @GetMapping("/members")
    public ApiResponse<List<Map<String, Object>>> getAllMembers() {
        return ApiResponse.success(sessionService.getAllMembers());
    }

    @GetMapping("/history/{username}")
    public ApiResponse<List<WorkSession>> getSessionHistory(@PathVariable String username) {
        return ApiResponse.success(sessionService.getSessionHistory(username));
    }

    @GetMapping("/active")
    public ApiResponse<List<WorkSession>> getActiveSessions() {
        return ApiResponse.success(sessionService.getActiveSessions());
    }
}
