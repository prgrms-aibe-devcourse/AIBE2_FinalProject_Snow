package com.example.popin.domain.mission.dto;

import com.example.popin.domain.mission.entity.UserMissionStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MissionSummaryDto {
    private UUID id;
    private String title;
    private String description;
    private UserMissionStatus userStatus;  // PENDING / SUCCESS / FAIL
}
