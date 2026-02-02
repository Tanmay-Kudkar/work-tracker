package com.worktracker.service;

import com.worktracker.dto.ActivityLogRequest;
import com.worktracker.dto.MemberSummaryDto;
import com.worktracker.exception.InvalidMemberException;
import com.worktracker.model.ActivityLog;
import com.worktracker.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;

    private static final Set<String> VALID_MEMBERS = Set.of(
            "tanmay_kudkar", "yash_thakur", "nidhish_vartak", "atharva_raut", "parth_waghe");

    private static final Map<String, String> MEMBER_NAMES = Map.of(
            "tanmay_kudkar", "Tanmay Kudkar",
            "yash_thakur", "Yash Thakur",
            "nidhish_vartak", "Nidhish Vartak",
            "atharva_raut", "Atharva Raut",
            "parth_waghe", "Parth Waghe");

    @Transactional
    public ActivityLog logActivity(ActivityLogRequest request) {
        validateMember(request.getUsername());

        LocalDateTime timestamp = LocalDateTime.now();
        if (request.getTimestamp() != null && !request.getTimestamp().isEmpty()) {
            try {
                timestamp = LocalDateTime.parse(request.getTimestamp());
            } catch (Exception e) {
                log.warn("Invalid timestamp format from client: {}, using server time", request.getTimestamp());
            }
        }

        ActivityLog activityLog = ActivityLog.builder()
                .username(request.getUsername())
                .applicationName(request.getApplicationName())
                .windowTitle(request.getWindowTitle())
                .timestamp(timestamp)
                .build();

        log.info("Logging activity for user: {}, app: {}",
                request.getUsername(), request.getApplicationName());

        return activityLogRepository.save(activityLog);
    }

    public List<MemberSummaryDto> getAllMembersSummary(LocalDate date) {
        return getAllMembersSummary(date, 0);
    }

    public List<MemberSummaryDto> getAllMembersSummary(LocalDate date, int tzOffsetMinutes) {
        LocalDateTime startOfDayUtc = localToUtc(date.atStartOfDay(), tzOffsetMinutes);
        LocalDateTime endOfDayUtc = localToUtc(date.atTime(LocalTime.MAX), tzOffsetMinutes);

        return VALID_MEMBERS.stream()
                .map(username -> createMemberSummary(username, startOfDayUtc, endOfDayUtc))
                .sorted(Comparator.comparing(MemberSummaryDto::getTotalActiveMinutes).reversed())
                .collect(Collectors.toList());
    }

    public Map<String, Object> getDashboard(String username, LocalDate date) {
        return getDashboard(username, date, 0);
    }

    public Map<String, Object> getDashboard(String username, LocalDate date, int tzOffsetMinutes) {
        validateMember(username);

        LocalDateTime startOfDayUtc = localToUtc(date.atStartOfDay(), tzOffsetMinutes);
        LocalDateTime endOfDayUtc = localToUtc(date.atTime(LocalTime.MAX), tzOffsetMinutes);

        List<ActivityLog> logs = activityLogRepository
                .findByUsernameAndTimestampBetweenOrderByTimestampAsc(username, startOfDayUtc, endOfDayUtc);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("username", username);
        dashboard.put("fullName", MEMBER_NAMES.getOrDefault(username, username));
        dashboard.put("date", date.toString());
        dashboard.put("totalActiveMinutes", calculateTotalActiveTime(logs));
        dashboard.put("topApplications", getTopApplications(logs));
        dashboard.put("hourlyActivity", getHourlyActivity(logs, tzOffsetMinutes));
        dashboard.put("categories", getCategoryBreakdown(logs));

        return dashboard;
    }

    /**
     * Stored timestamps are treated as UTC LocalDateTime. Convert a local datetime
     * (user/browser) to UTC.
     * tzOffsetMinutes is the user's offset from UTC in minutes (e.g. IST = +330).
     */
    private static LocalDateTime localToUtc(LocalDateTime local, int tzOffsetMinutes) {
        return local.minusMinutes(tzOffsetMinutes);
    }

    private static LocalDateTime utcToLocal(LocalDateTime utc, int tzOffsetMinutes) {
        return utc.plusMinutes(tzOffsetMinutes);
    }

    private MemberSummaryDto createMemberSummary(String username, LocalDateTime start, LocalDateTime end) {
        List<ActivityLog> logs = activityLogRepository
                .findByUsernameAndTimestampBetweenOrderByTimestampAsc(username, start, end);

        long totalMinutes = calculateTotalActiveTime(logs);
        String currentApp = logs.isEmpty() ? null : logs.get(logs.size() - 1).getApplicationName();
        boolean isActive = !logs.isEmpty() &&
                Duration.between(logs.get(logs.size() - 1).getTimestamp(), LocalDateTime.now()).toMinutes() < 2;

        return MemberSummaryDto.builder()
                .username(username)
                .fullName(MEMBER_NAMES.getOrDefault(username, username))
                .totalActiveMinutes(totalMinutes)
                .totalActiveHours(String.format("%.1f", totalMinutes / 60.0))
                .isActive(isActive)
                .currentApplication(isActive ? normalizeAppName(currentApp) : null)
                .topApp(getTopApp(logs))
                .build();
    }

    private void validateMember(String username) {
        if (username == null || !VALID_MEMBERS.contains(username.toLowerCase())) {
            throw new InvalidMemberException("Invalid team member: " + username);
        }
    }

    // Each log entry represents 30 seconds of activity
    private static final int SECONDS_PER_LOG = 30;

    private long calculateTotalActiveTime(List<ActivityLog> logs) {
        // Each log = 10 seconds, so total minutes = (logs * 10) / 60
        return (logs.size() * SECONDS_PER_LOG) / 60;
    }

    private List<Map<String, Object>> getTopApplications(List<ActivityLog> logs) {
        Map<String, Long> appCounts = logs.stream()
                .filter(log -> log.getApplicationName() != null)
                .collect(Collectors.groupingBy(
                        log -> normalizeAppName(log.getApplicationName()),
                        Collectors.counting()));

        return appCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> app = new HashMap<>();
                    long totalSeconds = entry.getValue() * SECONDS_PER_LOG;
                    app.put("name", entry.getKey());
                    app.put("minutes", totalSeconds / 60);
                    app.put("seconds", totalSeconds % 60);
                    app.put("percentage", logs.isEmpty() ? 0 : (entry.getValue() * 100.0 / logs.size()));
                    return app;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getHourlyActivity(List<ActivityLog> logs, int tzOffsetMinutes) {
        Map<Integer, Long> hourlyCount = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> utcToLocal(log.getTimestamp(), tzOffsetMinutes).getHour(),
                        Collectors.counting()));

        List<Map<String, Object>> hourly = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            Map<String, Object> h = new HashMap<>();
            h.put("hour", hour);
            h.put("label", String.format("%02d:00", hour));
            long count = hourlyCount.getOrDefault(hour, 0L);
            h.put("minutes", (count * SECONDS_PER_LOG) / 60);
            h.put("active", count > 0);
            hourly.add(h);
        }
        return hourly;
    }

    private Map<String, Object> getCategoryBreakdown(List<ActivityLog> logs) {
        Map<String, Long> categories = new HashMap<>();

        for (ActivityLog log : logs) {
            String app = log.getApplicationName() != null ? log.getApplicationName().toLowerCase() : "";
            String title = log.getWindowTitle() != null ? log.getWindowTitle().toLowerCase() : "";
            String category = categorizeActivity(app, title);
            categories.merge(category, 1L, Long::sum);
        }

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> categoryList = categories.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> cat = new HashMap<>();
                    cat.put("name", entry.getKey());
                    cat.put("minutes", (entry.getValue() * SECONDS_PER_LOG) / 60);
                    cat.put("percentage", logs.isEmpty() ? 0 : (entry.getValue() * 100.0 / logs.size()));
                    cat.put("color", getCategoryColor(entry.getKey()));
                    return cat;
                })
                .collect(Collectors.toList());

        result.put("categories", categoryList);
        result.put("tree", buildCategoryTree(logs));
        return result;
    }

    private List<Map<String, Object>> buildCategoryTree(List<ActivityLog> logs) {
        Map<String, Map<String, Long>> tree = new HashMap<>();

        for (ActivityLog log : logs) {
            String app = log.getApplicationName() != null ? log.getApplicationName().toLowerCase() : "";
            String title = log.getWindowTitle() != null ? log.getWindowTitle() : "";
            String category = categorizeActivity(app, title);
            String normalizedApp = normalizeAppName(log.getApplicationName());

            tree.computeIfAbsent(category, k -> new HashMap<>())
                    .merge(normalizedApp, 1L, Long::sum);
        }

        return tree.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> cat = new HashMap<>();
                    cat.put("category", entry.getKey());
                    cat.put("color", getCategoryColor(entry.getKey()));

                    long totalCount = entry.getValue().values().stream().mapToLong(Long::longValue).sum();
                    cat.put("totalMinutes", (totalCount * SECONDS_PER_LOG) / 60);

                    List<Map<String, Object>> apps = entry.getValue().entrySet().stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .map(appEntry -> {
                                Map<String, Object> a = new HashMap<>();
                                a.put("name", appEntry.getKey());
                                a.put("minutes", (appEntry.getValue() * SECONDS_PER_LOG) / 60);
                                return a;
                            })
                            .collect(Collectors.toList());
                    cat.put("applications", apps);

                    return cat;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("totalMinutes"),
                        (Long) a.get("totalMinutes")))
                .collect(Collectors.toList());
    }

    private String getTopApp(List<ActivityLog> logs) {
        return logs.stream()
                .filter(log -> log.getApplicationName() != null)
                .collect(Collectors.groupingBy(
                        log -> normalizeAppName(log.getApplicationName()),
                        Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String categorizeActivity(String app, String title) {
        if (app.contains("code") || app.contains("idea") || app.contains("intellij") ||
                app.contains("visual studio") || app.contains("eclipse") || app.contains("pycharm")) {
            return "Programming";
        }
        if (app.contains("zoom") || app.contains("teams") || app.contains("slack") ||
                app.contains("discord") || title.contains("meeting")) {
            return "Communication";
        }
        if (app.contains("chrome") || app.contains("firefox") || app.contains("edge") || app.contains("browser")) {
            if (title.contains("github") || title.contains("stackoverflow") || title.contains("documentation")) {
                return "Programming";
            }
            if (title.contains("youtube") || title.contains("netflix") || title.contains("twitch")) {
                return "Entertainment";
            }
            if (title.contains("mail") || title.contains("gmail") || title.contains("outlook")) {
                return "Email";
            }
            return "Browsing";
        }
        if (app.contains("terminal") || app.contains("cmd") || app.contains("powershell") ||
                app.contains("bash") || app.contains("windowsterminal")) {
            return "Programming";
        }
        if (app.contains("explorer")) {
            return "File Management";
        }
        if (app.contains("minecraft") || app.contains("steam") || app.contains("game")) {
            return "Games";
        }
        if (app.contains("spotify") || app.contains("vlc") || app.contains("music")) {
            return "Media";
        }
        return "Other";
    }

    private String getCategoryColor(String category) {
        return switch (category) {
            case "Programming" -> "#22c55e";
            case "Communication" -> "#3b82f6";
            case "Browsing" -> "#f59e0b";
            case "Entertainment" -> "#ef4444";
            case "Email" -> "#8b5cf6";
            case "File Management" -> "#06b6d4";
            case "Games" -> "#ec4899";
            case "Media" -> "#f97316";
            default -> "#64748b";
        };
    }

    private String normalizeAppName(String appName) {
        if (appName == null)
            return "Unknown";
        String lower = appName.toLowerCase();
        if (lower.contains("code"))
            return "VS Code";
        if (lower.contains("idea") || lower.contains("intellij"))
            return "IntelliJ IDEA";
        if (lower.contains("chrome"))
            return "Chrome";
        if (lower.contains("firefox"))
            return "Firefox";
        if (lower.contains("edge"))
            return "Edge";
        if (lower.contains("terminal") || lower.contains("powershell") || lower.contains("cmd"))
            return "Terminal";
        if (lower.contains("explorer"))
            return "File Explorer";
        if (lower.contains("zoom"))
            return "Zoom";
        if (lower.contains("teams"))
            return "Teams";
        if (lower.contains("slack"))
            return "Slack";
        if (lower.contains("discord"))
            return "Discord";
        if (lower.contains("spotify"))
            return "Spotify";
        return appName.replace(".exe", "").replace(".EXE", "");
    }

    private String truncateTitle(String title) {
        if (title == null)
            return "";
        if (title.length() > 60) {
            return title.substring(0, 57) + "...";
        }
        return title;
    }
}
