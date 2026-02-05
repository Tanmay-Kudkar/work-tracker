package com.worktracker.service;

import com.worktracker.model.ActivityLog;
import com.worktracker.model.TeamMember;
import com.worktracker.repository.ActivityLogRepository;
import com.worktracker.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdleDetectionService {

    private final TeamMemberRepository teamMemberRepository;
    private final ActivityLogRepository activityLogRepository;

    // Idle threshold: 5 minutes (matches tracker.py)
    @Value("${worktracker.idle.threshold.minutes:5}")
    private int idleThresholdMinutes;

    /**
     * Check every minute if any active members should be marked as offline
     * due to no activity for 5+ minutes
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    @Transactional
    public void checkForIdleMembers() {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime idleThreshold = nowUtc.minusMinutes(idleThresholdMinutes);

        List<TeamMember> activeMembers = teamMemberRepository.findByIsCurrentlyWorking(true);

        for (TeamMember member : activeMembers) {
            // Find the most recent activity for this member
            List<ActivityLog> recentLogs = activityLogRepository
                    .findByUsernameAndTimestampBetweenOrderByTimestampAsc(
                            member.getUsername(),
                            idleThreshold,
                            nowUtc.plusMinutes(1)
                    );

            // If no activity in last 5 minutes, mark as offline
            if (recentLogs.isEmpty()) {
                log.info("Auto-marking {} as offline - no activity for {} minutes",
                        member.getUsername(), idleThresholdMinutes);
                
                member.setIsCurrentlyWorking(false);
                member.setCurrentApplication(null);
                teamMemberRepository.save(member);
            }
        }
    }
}
