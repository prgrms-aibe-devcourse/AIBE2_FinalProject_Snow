package com.example.popin.domain.missionset.dto;

import com.example.popin.domain.usermission.UserMissionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissionSummaryDto {
    private Long id;
    private String title;
    private String description;
    private UserMissionStatus userStatus;  // PENDING / SUCCESS / FAIL
}
