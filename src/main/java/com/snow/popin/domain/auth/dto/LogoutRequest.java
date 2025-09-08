package com.snow.popin.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    private String accessToken;
    private String refreshToken; // 향후 확장용
}