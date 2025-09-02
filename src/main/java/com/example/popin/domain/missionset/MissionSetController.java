package com.example.popin.domain.missionset;

import com.example.popin.domain.missionset.dto.MissionSetViewDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mission-sets")
public class MissionSetController {

    private final MissionSetService missionSetService;

    public MissionSetController(MissionSetService missionSetService) {
        this.missionSetService = missionSetService;
    }

    @GetMapping("/by-popup/{popupId}")
    public List<MissionSetViewDto> byPopup(@PathVariable Long popupId,
                                           @RequestParam(required = false) Long userId) {
        return missionSetService.getByPopup(popupId, userId);
    }
}
