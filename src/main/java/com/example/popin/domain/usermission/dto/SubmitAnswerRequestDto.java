package com.example.popin.domain.usermission.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SubmitAnswerRequestDto {
    @NotNull
    private Long userId;

    @NotBlank
    private String answer;
}
