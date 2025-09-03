package com.example.popin.domain.reward.dto.request;

import lombok.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ClaimRequestDto {
    @NotNull private UUID missionSetId;
    @NotNull private Long optionId;
}
