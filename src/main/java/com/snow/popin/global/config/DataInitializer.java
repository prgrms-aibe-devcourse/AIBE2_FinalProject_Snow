package com.snow.popin.global.config;

import com.example.popin.domain.auth.constant.AuthProvider;
import com.example.popin.domain.user.UserRepository;
import com.example.popin.domain.user.constant.Role;
import com.example.popin.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        // 데이터가 이미 있으면 실행하지 않음
        if (userRepository.count() > 0) {
            log.info("더미 데이터가 이미 존재하여 생성하지 않습니다.");
            return;
        }

        log.info("개발용 더미 데이터를 생성합니다.");

        User user1 = User.builder()
                .email("user1@test.com")
                .password(passwordEncoder.encode("1234"))
                .name("유저1이름")
                .nickname("유저닉네임1")
                .phone("010-1234-1234")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        User user2 = User.builder()
                .email("user2@test.com")
                .password(passwordEncoder.encode("1234"))
                .name("유저2이름")
                .nickname("유저닉네임2")
                .phone("010-5678-5678")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        userRepository.saveAll(Arrays.asList(user1, user2));


    }
}
