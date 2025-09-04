package com.example.popin.domain.mission.controller;

import com.example.popin.domain.mission.dto.MissionSetViewDto;
import com.example.popin.domain.mission.service.MissionSetService;
import com.example.popin.domain.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mission-sets")
public class MissionSetController {

    private final MissionSetService missionSetService;
    private final UserService userService;

    public MissionSetController(MissionSetService missionSetService, UserService userService) {
        this.missionSetService = missionSetService;
        this.userService = userService;
    }

    @GetMapping("/{missionSetId}")
    public MissionSetViewDto byMissionSet(@PathVariable UUID missionSetId, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("인증된 사용자가 없습니다.");
        }

        Long userId = userService.getUserIdByUsername(principal.getName());
        if (userId == null) {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + principal.getName());
        }

        return missionSetService.getOne(missionSetId, userId);
    }

}
