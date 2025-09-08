package com.snow.popin.domain.user.dto;

import com.snow.popin.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private final String name;
    private final String nickname;
    private final String email;
    private final String phone;

    public UserResponseDto(User user) {
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.phone = user.getPhone();
    }
}
