package com.worktracker.repository;

import com.worktracker.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByUsername(String username);

    List<TeamMember> findAllByOrderByTotalWorkingMinutesDesc();
    
    List<TeamMember> findByIsCurrentlyWorking(Boolean isCurrentlyWorking);
}
