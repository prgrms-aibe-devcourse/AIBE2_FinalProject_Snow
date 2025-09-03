package com.example.popin.domain.mission.controller;

import com.example.popin.domain.mission.dto.MissionSetViewDto;
import com.example.popin.domain.mission.service.MissionSetService;
import com.example.popin.domain.user.UserService;
import org.springframework.web.bind.annotation.*;

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
    public List<MissionSetViewDto> byPopup(@PathVariable Long popupId, java.security.Principal principal) {
        Long userId = 1L; //TODO: 실제 userid값 받아오도록 수정 필요
        //if (principal != null) {
        //    userId = userService.getUserIdByUsername(principal.getName());
        //}
        return missionSetService.getByPopup(popupId, userId);
    }
}
