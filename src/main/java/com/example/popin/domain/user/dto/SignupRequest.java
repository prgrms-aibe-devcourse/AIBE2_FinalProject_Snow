package com.example.popin.domain.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {

    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;

}
