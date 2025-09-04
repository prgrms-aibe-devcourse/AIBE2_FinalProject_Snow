package com.example.popin.domain.user.entity;

import com.example.popin.domain.auth.constant.AuthProvider;
import com.example.popin.domain.user.constant.Role;
import com.example.popin.global.common.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String nickname;

    private String phone;

    @Column(name = "auth_provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public User(String email, String password, String name, String nickname,
                String phone, AuthProvider authProvider, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.authProvider = authProvider;
        this.role = role != null ? role : Role.USER;
    }

    // 비즈니스 로직 메소드들
    public void updateProfile(String name, String nickname, String phone) {
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
}