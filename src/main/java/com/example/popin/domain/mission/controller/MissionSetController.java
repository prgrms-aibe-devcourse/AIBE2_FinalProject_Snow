package com.example.popin.domain.mission.controller;

import com.example.popin.domain.mission.dto.MissionSetViewDto;
import com.example.popin.domain.mission.service.MissionSetService;
import com.example.popin.domain.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/mission-sets")
public class MissionSetController {

    private final MissionSetService missionSetService;
    private final UserService userService;

    public MissionSetController(MissionSetService missionSetService, UserService userService) {
        this.missionSetService = missionSetService;
        this.userService = userService;
    }

    @GetMapping("/by-popup/{popupId}")
    public List<MissionSetViewDto> byPopup(@PathVariable Long popupId, Principal principal) {
        Long userId = null;

        if (principal != null) {
            userId = userService.getUserIdByUsername(principal.getName());
        }

        if (userId == null) {
            throw new IllegalStateException("로그인한 사용자 정보를 찾을 수 없습니다.");
        }

        return missionSetService.getByPopup(popupId, userId);
    }
}
