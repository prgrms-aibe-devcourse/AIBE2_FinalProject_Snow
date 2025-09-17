package com.snow.popin.domain.mission.dto;

import lombok.Getter;

@Getter
public class MissionSetCreateRequestDto {
    private Long popupId;
    private Integer requiredCount;
    private String status;
    private String rewardPin;
}