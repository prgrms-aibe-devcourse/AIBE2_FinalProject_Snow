package com.example.popin.domain.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MissionDto {
    private Long id;
    private String title;
    private String description;
    private Long missionSetId; // 미션이 속한 세트 id
}
