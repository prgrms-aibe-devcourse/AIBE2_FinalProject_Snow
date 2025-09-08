package com.snow.popin.domain.auth.dto;

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
