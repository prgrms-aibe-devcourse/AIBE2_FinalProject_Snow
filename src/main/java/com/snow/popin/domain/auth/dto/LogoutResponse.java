package com.snow.popin.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutResponse {

    private String message;
    private boolean success;

    public static LogoutResponse success(String message){
        return LogoutResponse.builder()
                .message(message)
                .success(true)
                .build();
    }

    public static LogoutResponse failure(String message){
        return LogoutResponse.builder()
                .message(message)
                .success(false)
                .build();
    }
}
