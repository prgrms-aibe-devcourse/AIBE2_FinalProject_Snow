package com.snow.popin.domain.mission.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ActiveMissionSetDto {
    private UUID missionSetId;
    private String missionSetTitle;
    private String thumbnailUrl; // 미션셋 대표 이미지
    private int totalMissions;
    private int completedMissions;
    private boolean completed;   // 전체 완료 여부
}
