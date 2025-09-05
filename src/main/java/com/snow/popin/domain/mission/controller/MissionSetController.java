package com.snow.popin.domain.mission.controller;

import com.snow.popin.domain.mission.dto.MissionSetViewDto;
import com.snow.popin.domain.mission.service.MissionSetService;
import com.snow.popin.domain.user.UserService;
import com.snow.popin.global.exception.MissionException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mission-sets")
public class MissionSetController {

    private final MissionSetService missionSetService;
    private final UserService userService;

    @GetMapping("/{missionSetId}")
    public MissionSetViewDto byMissionSet(@PathVariable UUID missionSetId, Principal principal) {
        //TODO: 사용자 받아오기 config로 대체 예정
        if (principal == null) {
            throw new MissionException.Unauthorized("인증된 사용자가 없습니다.");
        }
        Long userId = userService.getUserIdByUsername(principal.getName());
        if (userId == null) {
            throw new MissionException.Unauthorized("해당 사용자를 찾을 수 없습니다: " + principal.getName());
        }

        return missionSetService.getOne(missionSetId, userId);
    }
}
