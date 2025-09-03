package com.example.popin.domain.mission.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class SubmitAnswerRequestDto {
    @NotBlank
    private String answer;
}
