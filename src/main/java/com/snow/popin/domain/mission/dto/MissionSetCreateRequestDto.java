package com.snow.popin.domain.mission.dto;

import com.snow.popin.domain.mission.entity.MissionSetStatus;
import lombok.Getter;

@Getter
public class MissionSetCreateRequestDto {
    private Long popupId;
    private Integer requiredCount;
    private MissionSetStatus status;
    private String rewardPin;
}