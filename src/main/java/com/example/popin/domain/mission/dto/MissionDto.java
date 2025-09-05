package com.example.popin.domain.mission.dto;

import com.example.popin.domain.mission.entity.Mission;
import lombok.Builder;
import lombok.Getter;


import java.util.UUID;

@Getter
@Builder
public class MissionDto {
    private final UUID id;
    private final String title;
    private final String description;
    private final UUID missionSetId;

    public static MissionDto from(Mission m) {
        UUID msId = (m.getMissionSet() != null) ? m.getMissionSet().getId() : null;
        return MissionDto.builder()
                .id(m.getId())
                .title(m.getTitle())
                .description(m.getDescription())
                .missionSetId(msId)
                .build();
    }
}
