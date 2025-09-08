package com.snow.popin.domain.mission.controller;

import com.snow.popin.domain.mission.entity.UserMission;
import com.snow.popin.domain.mission.service.UserMissionService;
import com.snow.popin.domain.user.service.UserService;
import com.snow.popin.domain.mission.dto.SubmitAnswerRequestDto;
import com.snow.popin.domain.mission.dto.SubmitAnswerResponseDto;
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

        //TODO: 사용자 받아오기 config로 대체 예정

        if (principal != null) {
            String name = principal.getName();
            userId = userService.getUserIdByUsername(name);
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
