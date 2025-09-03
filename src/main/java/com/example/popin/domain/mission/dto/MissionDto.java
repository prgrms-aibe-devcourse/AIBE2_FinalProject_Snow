package com.example.popin.domain.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MissionDto {
    private UUID id;
    private String title;
    private String description;
    private UUID missionSetId; // 미션이 속한 세트 id
}
