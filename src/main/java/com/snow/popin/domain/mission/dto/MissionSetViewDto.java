package com.snow.popin.domain.mission.dto;

import com.snow.popin.domain.mission.entity.MissionSet;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MissionSetViewDto {

    private final UUID missionSetId;
    private final Long popupId;
    private final Integer requiredCount;
    private final List<MissionSummaryDto> missions;
    private final int totalMissions;

    private final Long userId;
    private final Long successCount;
    private final Boolean cleared;

    public static MissionSetViewDto from(MissionSet set, Long userId,
                                         List<MissionSummaryDto> missions,
                                         Long successCount, boolean cleared) {
        List<MissionSummaryDto> safeMissions =
                (missions == null) ? Collections.emptyList() : missions;
        int totalMissions = safeMissions.size();

        return MissionSetViewDto.builder()
                .missionSetId(set.getId())
                .popupId(set.getPopupId())
                .requiredCount(set.getRequiredCount())
                .missions(safeMissions)
                .totalMissions(totalMissions)
                .userId(userId)
                .successCount(successCount)
                .cleared(cleared)
                .build();
    }


}
