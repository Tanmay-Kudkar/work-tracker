package com.worktracker.controller;

import com.worktracker.dto.ApiResponse;
import com.worktracker.model.TeamMember;
import com.worktracker.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final TeamMemberRepository teamMemberRepository;

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("username missing"));
        }

        TeamMember member = teamMemberRepository.findByUsername(username).orElse(null);
        if (member != null) {
            member.setIsCurrentlyWorking(false);
            member.setCurrentApplication(null);
            teamMemberRepository.save(member);
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "ok")));
    }
}
