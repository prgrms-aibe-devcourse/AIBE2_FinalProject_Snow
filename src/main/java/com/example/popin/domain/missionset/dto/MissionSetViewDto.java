package com.example.popin.domain.missionset.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class MissionSetViewDto {
    private UUID missionSetId;
    private Long popupId;
    private Integer requiredCount;   // 세트 클리어에 필요한 성공 수(없으면 null)
    private int totalMissions;       // 미션 개수
    private Long userId;
    private long successCount;       // userId 있으면 채움
    private boolean cleared;         // successCount >= requiredCount
    private List<MissionSummaryDto> missions;
}
