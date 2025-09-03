package com.example.popin.domain.mission.controller;

import com.example.popin.domain.mission.entity.UserMission;
import com.example.popin.domain.mission.service.UserMissionService;
import com.example.popin.domain.user.UserService;
import com.example.popin.domain.mission.dto.SubmitAnswerRequestDto;
import com.example.popin.domain.mission.dto.SubmitAnswerResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-missions")
@Validated
public class UserMissionController {

    private final UserMissionService userMissionService;
    private final UserService userService;

    public UserMissionController(UserMissionService userMissionService, UserService userService) {
        this.userMissionService = userMissionService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserMission> create(@RequestBody UserMission userMission) {
        return ResponseEntity.ok(userMissionService.create(userMission));
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
            userId = userService.getUserIdByUsername(principal.getName());
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
