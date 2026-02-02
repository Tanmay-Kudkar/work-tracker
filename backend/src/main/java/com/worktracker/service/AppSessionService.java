package com.worktracker.service;

import com.worktracker.dto.AppSessionDto;
import com.worktracker.dto.SessionEventRequest;
import com.worktracker.exception.InvalidMemberException;
import com.worktracker.model.AppSession;
import com.worktracker.repository.AppSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppSessionService {

    private final AppSessionRepository appSessionRepository;

    private static final Set<String> VALID_MEMBERS = Set.of(
            "tanmay_kudkar", "yash_thakur", "nidhish_vartak", "atharva_raut", "parth_waghe");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Transactional
    public AppSession handleSessionEvent(SessionEventRequest request) {
        validateMember(request.getUsername());

        String eventType = request.getEventType().toLowerCase();

        switch (eventType) {
            case "start":
                return startSession(request);
            case "end":
                return endSession(request);
            case "heartbeat":
                return heartbeat(request);
            default:
                throw new IllegalArgumentException("Invalid event type: " + eventType);
        }
    }

    private AppSession startSession(SessionEventRequest request) {
        Optional<AppSession> existing = appSessionRepository
                .findByUsernameAndApplicationNameAndIsActiveTrue(
                        request.getUsername(), request.getApplicationName());

        if (existing.isPresent()) {
            return existing.get();
        }

        AppSession session = AppSession.builder()
                .username(request.getUsername())
                .applicationName(normalizeAppName(request.getApplicationName()))
                .processName(request.getProcessName())
                .startTime(LocalDateTime.now())
                .isActive(true)
                .build();

        log.info("Started app session: {} - {}", request.getUsername(), request.getApplicationName());
        return appSessionRepository.save(session);
    }

    private AppSession endSession(SessionEventRequest request) {
        Optional<AppSession> sessionOpt = appSessionRepository
                .findByUsernameAndApplicationNameAndIsActiveTrue(
                        request.getUsername(), request.getApplicationName());

        if (sessionOpt.isEmpty()) {
            return null;
        }

        AppSession session = sessionOpt.get();
        session.setEndTime(LocalDateTime.now());
        session.setActive(false);
        session.setEndReason(request.getEndReason() != null ? request.getEndReason() : "normal");
        session.setDurationSeconds(Duration.between(session.getStartTime(), session.getEndTime()).getSeconds());

        log.info("Ended app session: {} - {} ({})",
                request.getUsername(), request.getApplicationName(), session.getEndReason());
        return appSessionRepository.save(session);
    }

    private AppSession heartbeat(SessionEventRequest request) {
        Optional<AppSession> sessionOpt = appSessionRepository
                .findByUsernameAndApplicationNameAndIsActiveTrue(
                        request.getUsername(), request.getApplicationName());

        return sessionOpt.orElseGet(() -> startSession(request));
    }

    public List<AppSessionDto> getSessionsForDate(String username, LocalDate date) {
        validateMember(username);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return appSessionRepository
                .findByUsernameAndStartTimeBetweenOrderByStartTimeDesc(username, startOfDay, endOfDay)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AppSessionDto> getActiveSessions(String username) {
        validateMember(username);

        return appSessionRepository.findByUsernameAndIsActiveTrueOrderByStartTimeDesc(username)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getSessionsSummary(String username, LocalDate date) {
        validateMember(username);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<AppSession> sessions = appSessionRepository
                .findByUsernameAndStartTimeBetweenOrderByStartTimeDesc(username, startOfDay, endOfDay);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSessions", sessions.size());
        summary.put("activeSessions", sessions.stream().filter(AppSession::isActive).count());
        summary.put("normalEnds", sessions.stream().filter(s -> "normal".equals(s.getEndReason())).count());
        summary.put("killedEnds", sessions.stream().filter(s -> "killed".equals(s.getEndReason())).count());
        summary.put("sessions", sessions.stream().limit(50).map(this::toDto).collect(Collectors.toList()));

        // Group by app
        Map<String, Long> appSessionCounts = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> normalizeAppName(s.getApplicationName()),
                        Collectors.counting()));

        summary.put("appCounts", appSessionCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> Map.of("name", e.getKey(), "count", e.getValue()))
                .collect(Collectors.toList()));

        return summary;
    }

    private AppSessionDto toDto(AppSession session) {
        return AppSessionDto.builder()
                .id(session.getId())
                .applicationName(normalizeAppName(session.getApplicationName()))
                .processName(session.getProcessName())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .isActive(session.isActive())
                .endReason(session.getEndReason())
                .durationSeconds(session.getDurationSeconds())
                .formattedDuration(formatDuration(session))
                .formattedStartTime(
                        session.getStartTime() != null ? session.getStartTime().format(TIME_FORMATTER) : null)
                .formattedEndTime(session.getEndTime() != null ? session.getEndTime().format(TIME_FORMATTER) : null)
                .build();
    }

    private String formatDuration(AppSession session) {
        if (session.getDurationSeconds() != null) {
            long seconds = session.getDurationSeconds();
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;

            if (hours > 0) {
                return String.format("%dh %dm %ds", hours, minutes, secs);
            } else if (minutes > 0) {
                return String.format("%dm %ds", minutes, secs);
            } else {
                return String.format("%ds", secs);
            }
        } else if (session.isActive()) {
            long seconds = Duration.between(session.getStartTime(), LocalDateTime.now()).getSeconds();
            long minutes = seconds / 60;
            if (minutes > 0) {
                return String.format("%dm (running)", minutes);
            } else {
                return "Just started";
            }
        }
        return "N/A";
    }

    private String normalizeAppName(String appName) {
        if (appName == null)
            return "Unknown";
        return appName.replace(".exe", "").replace("_", " ").trim();
    }

    private void validateMember(String username) {
        if (username == null || !VALID_MEMBERS.contains(username.toLowerCase())) {
            throw new InvalidMemberException("Invalid team member: " + username);
        }
    }
}
