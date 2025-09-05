package com.example.popin.domain.mission.dto;

import com.example.popin.domain.mission.entity.UserMissionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
public class SubmitAnswerResponseDto {
    private boolean pass;
    private UserMissionStatus status;
    private UUID missionSetId;
    private long successCount;
    private Integer requiredCount;
    private boolean cleared;

    public static SubmitAnswerResponseDto from(boolean pass,
                                               UserMissionStatus status,
                                               UUID missionSetId,
                                               long successCount,
                                               Integer requiredCount,
                                               boolean cleared) {
        return SubmitAnswerResponseDto.builder()
                .pass(pass)
                .status(status)
                .missionSetId(missionSetId)
                .successCount(successCount)
                .requiredCount(requiredCount)
                .cleared(cleared)
                .build();
    }
}
