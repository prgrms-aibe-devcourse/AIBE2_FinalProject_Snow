package com.example.popin.domain.reward.dto.request;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class RedeemRequestDto {
    @NotNull  private UUID missionSetId;
    @NotBlank private String staffPin;
}