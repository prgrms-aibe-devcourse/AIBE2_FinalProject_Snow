package com.example.popin.domain.mission.controller;

import com.example.popin.domain.mission.entity.UserMission;
import com.example.popin.domain.mission.service.UserMissionService;
import com.example.popin.domain.user.UserService;
import com.example.popin.domain.mission.dto.SubmitAnswerRequestDto;
import com.example.popin.domain.mission.dto.SubmitAnswerResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-missions")
@Validated
public class UserMissionController {

    private final UserMissionService userMissionService;
    private final UserService userService;

    @PostMapping("/{missionId}")
    public ResponseEntity<UserMission> create(
            @PathVariable UUID missionId,
            Principal principal
    ) {
        Long userId = userService.getUserIdByUsername(principal.getName());
        UserMission userMission = userMissionService.create(userId, missionId);
        return ResponseEntity.ok(userMission);
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserMission> get(@PathVariable Long id) {
        Optional<UserMission> found = userMissionService.findById(id);
        if (found.isPresent()) return ResponseEntity.ok(found.get());
        return ResponseEntity.notFound().build();
    }


    @PostMapping("/{missionId}/submit-answer")
    public ResponseEntity<SubmitAnswerResponseDto> submitAnswer(
            @PathVariable UUID missionId,
            Principal principal,
            @RequestBody @Valid SubmitAnswerRequestDto req
    ) {
        Long userId = null;


        if (principal != null) {
            String name = principal.getName();
            System.out.println("[DEBUG] principal.getName() = " + name);
            userId = userService.getUserIdByUsername(name);
            System.out.println("[DEBUG] userId from DB = " + userId);
        }

        // 인증 필요
        if (principal == null || userId == null) {
            return ResponseEntity
                .status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .build();
        }

        SubmitAnswerResponseDto res = userMissionService.submitAnswer(missionId, userId, req.getAnswer());
        return ResponseEntity.ok(res);
    }


}
