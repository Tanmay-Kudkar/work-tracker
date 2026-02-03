package com.worktracker.service;

import com.worktracker.dto.ApiResponse;
import com.worktracker.dto.SessionEventRequest;
import com.worktracker.exception.InvalidMemberException;
import com.worktracker.model.TeamMember;
import com.worktracker.model.WorkSession;
import com.worktracker.repository.TeamMemberRepository;
import com.worktracker.repository.WorkSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final WorkSessionRepository sessionRepository;
    private final TeamMemberRepository memberRepository;

    private static final Set<String> VALID_MEMBERS = Set.of(
            "tanmay_kudkar", "yash_thakur", "nidhish_vartak", "atharva_raut", "parth_waghe");

    private static final Set<String> VALID_APPS = Set.of(
            "Code.exe", "code", "idea64.exe", "idea", "intellij");

    private static final Map<String, String> FULL_NAMES = Map.of(
            "tanmay_kudkar", "Tanmay Kudkar",
            "yash_thakur", "Yash Thakur",
            "nidhish_vartak", "Nidhish Vartak",
            "atharva_raut", "Atharva Raut",
            "parth_waghe", "Parth Waghe");

    @Transactional
    public ApiResponse<Map<String, String>> processSessionEvent(SessionEventRequest request) {
        if (request.getUsername() == null || !VALID_MEMBERS.contains(request.getUsername().toLowerCase())) {
            throw new InvalidMemberException("Invalid team member: " + request.getUsername());
        }

        String eventType = request.getEventType().toLowerCase();
        log.info("Processing session event: {} for {} - {}", eventType, request.getUsername(), request.getApplicationName());

        TeamMember member = memberRepository.findByUsername(request.getUsername())
                .orElseGet(() -> createMember(request.getUsername()));

        switch (eventType) {
            case "start":
                handleSessionStart(member, request.getApplicationName());
                break;
            case "end":
                handleSessionEnd(member, request.getApplicationName());
                break;
            case "heartbeat":
                handleSessionHeartbeat(member, request.getApplicationName());
                break;
            default:
                log.warn("Unknown event type: {}", eventType);
        }

        return ApiResponse.success(Map.of("status", "ok"));
    }

    private void handleSessionStart(TeamMember member, String appName) {
        // Mark member as currently working
        member.setIsCurrentlyWorking(true);
        member.setCurrentApplication(normalizeAppName(appName));
        memberRepository.save(member);
        log.info("User {} started working on {}", member.getUsername(), appName);
    }

    private void handleSessionEnd(TeamMember member, String appName) {
        // Mark member as not working
        member.setIsCurrentlyWorking(false);
        member.setCurrentApplication(null);
        memberRepository.save(member);
        log.info("User {} stopped working on {}", member.getUsername(), appName);
    }

    private void handleSessionHeartbeat(TeamMember member, String appName) {
        // Update current application if needed
        String normalizedApp = normalizeAppName(appName);
        if (!normalizedApp.equals(member.getCurrentApplication())) {
            member.setCurrentApplication(normalizedApp);
        }
        member.setIsCurrentlyWorking(true);
        memberRepository.save(member);
    }

    @Transactional
    public ApiResponse<Map<String, String>> processHeartbeat(String username, String appName) {
        if (username == null || !VALID_MEMBERS.contains(username.toLowerCase())) {
            throw new InvalidMemberException("Invalid team member: " + username);
        }

        boolean isValidApp = VALID_APPS.stream()
                .anyMatch(app -> appName != null && appName.toLowerCase().contains(app.toLowerCase()));

        TeamMember member = memberRepository.findByUsername(username)
                .orElseGet(() -> createMember(username));

        if (isValidApp) {
            String normalizedApp = normalizeAppName(appName);
            handleActiveSession(member, normalizedApp);
        } else {
            endActiveSession(member);
        }

        return ApiResponse.success(Map.of("status", "ok"));
    }

    @Transactional
    public ApiResponse<Map<String, String>> processLogout(String username) {
        if (username == null) {
            throw new InvalidMemberException("Username required");
        }

        memberRepository.findByUsername(username)
                .ifPresent(this::endActiveSession);

        return ApiResponse.success(Map.of("status", "logged out"));
    }

    public List<Map<String, Object>> getAllMembers() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (String username : VALID_MEMBERS) {
            TeamMember member = memberRepository.findByUsername(username)
                    .orElseGet(() -> createMember(username));

            Long totalMinutes = Optional.ofNullable(
                    sessionRepository.getTotalMinutesByUsername(username)).orElse(0L);

            // Add current active session time
            Optional<WorkSession> activeSession = sessionRepository.findByUsernameAndIsActiveTrue(username);
            if (activeSession.isPresent()) {
                long activeMinutes = Duration.between(
                        activeSession.get().getLoginTime(), LocalDateTime.now()).toMinutes();
                totalMinutes += activeMinutes;
            }

            Map<String, Object> memberData = new LinkedHashMap<>();
            memberData.put("username", username);
            memberData.put("fullName", member.getFullName());
            memberData.put("totalWorkingMinutes", totalMinutes);
            memberData.put("totalWorkingHours", String.format("%.1f", totalMinutes / 60.0));
            memberData.put("isCurrentlyWorking",
                    Boolean.TRUE.equals(member.getIsCurrentlyWorking()));
            memberData.put("currentApplication", member.getCurrentApplication());

            result.add(memberData);
        }

        result.sort((a, b) -> Long.compare(
                (Long) b.get("totalWorkingMinutes"),
                (Long) a.get("totalWorkingMinutes")));

        return result;
    }

    public List<WorkSession> getSessionHistory(String username) {
        return sessionRepository.findByUsernameOrderByLoginTimeDesc(username);
    }

    public List<WorkSession> getActiveSessions() {
        return sessionRepository.findByIsActiveTrue();
    }

    // Private helper methods

    private TeamMember createMember(String username) {
        TeamMember member = TeamMember.builder()
                .username(username)
                .fullName(FULL_NAMES.getOrDefault(username.toLowerCase(), username))
                .totalWorkingMinutes(0L)
                .isCurrentlyWorking(false)
                .build();
        return memberRepository.save(member);
    }

    private String normalizeAppName(String appName) {
        if (appName == null) return "Unknown";
        String lower = appName.toLowerCase();
        if (lower.contains("code")) return "VS Code";
        if (lower.contains("idea") || lower.contains("intellij")) return "IntelliJ IDEA";
        return appName;
    }

    private void handleActiveSession(TeamMember member, String appName) {
        Optional<WorkSession> activeSession = sessionRepository
                .findByUsernameAndIsActiveTrue(member.getUsername());

        if (activeSession.isPresent()) {
            WorkSession session = activeSession.get();
            if (!session.getApplicationName().equals(appName)) {
                endSession(session);
                startNewSession(member, appName);
            }
        } else {
            startNewSession(member, appName);
        }
    }

    private void startNewSession(TeamMember member, String appName) {
        WorkSession session = WorkSession.builder()
                .username(member.getUsername())
                .applicationName(appName)
                .loginTime(LocalDateTime.now())
                .isActive(true)
                .durationMinutes(0L)
                .build();
        sessionRepository.save(session);

        member.setIsCurrentlyWorking(true);
        member.setCurrentApplication(appName);
        memberRepository.save(member);
    }

    private void endActiveSession(TeamMember member) {
        sessionRepository.findByUsernameAndIsActiveTrue(member.getUsername())
                .ifPresent(this::endSession);

        member.setIsCurrentlyWorking(false);
        member.setCurrentApplication(null);
        memberRepository.save(member);
    }

    private void endSession(WorkSession session) {
        LocalDateTime logoutTime = LocalDateTime.now();
        session.setLogoutTime(logoutTime);
        session.setIsActive(false);
        session.setDurationMinutes(Duration.between(session.getLoginTime(), logoutTime).toMinutes());
        sessionRepository.save(session);
    }
}
