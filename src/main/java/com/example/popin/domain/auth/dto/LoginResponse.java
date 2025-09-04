package com.example.popin.domain.auth.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String name;
    private String role;

    public static LoginResponse of(String accessToken, Long userId, String email, String name, String role) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .name(name)
                .role(role)
                .build();
    }


}
