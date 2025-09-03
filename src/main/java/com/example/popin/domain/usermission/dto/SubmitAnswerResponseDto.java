package com.example.popin.domain.usermission.dto;

import com.example.popin.domain.usermission.UserMissionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SubmitAnswerResponseDto {

    private boolean isPass;
    private UserMissionStatus status;
    private UUID missionSetId;
    private long successCount;
    private Integer requiredCount;
    private boolean isCleared;
}
