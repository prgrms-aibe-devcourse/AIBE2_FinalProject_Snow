package com.snow.popin.global.config;

import com.snow.popin.domain.auth.constant.AuthProvider;
import com.snow.popin.domain.mission.entity.Mission;
import com.snow.popin.domain.mission.entity.MissionSet;
import com.snow.popin.domain.mission.repository.MissionRepository;
import com.snow.popin.domain.mission.repository.MissionSetRepository;
import com.snow.popin.domain.reward.entity.RewardOption;
import com.snow.popin.domain.reward.repository.RewardOptionRepository;
import com.snow.popin.domain.user.repository.UserRepository;
import com.snow.popin.domain.user.constant.Role;
import com.snow.popin.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MissionSetRepository missionSetRepository;
    private final MissionRepository missionRepository;
    private final RewardOptionRepository rewardOptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDummyUsers();
        createDummyMissions();
    }

    private void createDummyUsers() {
        if (userRepository.count() > 0) {
            log.info("유저 더미 데이터가 이미 존재하여 생성하지 않습니다.");
            return;
        }

        log.info("유저 더미 데이터를 생성합니다.");

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
        log.info("유저 더미 데이터 생성 완료");
    }

    private void createDummyMissions() {
        if (missionSetRepository.count() > 0) {
            MissionSet existing = missionSetRepository.findAll().get(0); // 첫 번째 미션셋
            log.info("미션셋 더미 데이터가 이미 존재합니다. 기존 MissionSet ID: {}", existing.getId());
            return;
        }

        log.info("미션셋 및 미션 더미 데이터를 생성합니다.");

        // 미션셋 (UUID는 JPA가 자동 생성)
        MissionSet missionSet = MissionSet.builder()
                .popupId(1001L)
                .requiredCount(3)
                .status("ACTIVE")
                .rewardPin("1234")
                .build();
        missionSetRepository.saveAndFlush(missionSet); // FK 보장 위해 flush

        // 미션 6개
        List<Mission> missions = Arrays.asList(
                Mission.builder().title("mission1").description("포토 스팟에서 사진 찍고 업로드").answer("photo").missionSet(missionSet).build(),
                Mission.builder().title("mission2").description("행사 부스에서 받은 리플렛 코드 입력").answer("CODE-2025").missionSet(missionSet).build(),
                Mission.builder().title("mission3").description("스태프가 알려준 암호 입력").answer("popin").missionSet(missionSet).build(),
                Mission.builder().title("mission4").description("전시 패널 A의 키워드 입력").answer("ART").missionSet(missionSet).build(),
                Mission.builder().title("mission5").description("퀴즈 정답 입력").answer("42").missionSet(missionSet).build(),
                Mission.builder().title("mission6").description("SNS 해시태그 확인 코드").answer("SNS").missionSet(missionSet).build()
        );
        missionRepository.saveAll(missions);

        // 리워드 옵션 4개
        List<RewardOption> options = Arrays.asList(
                RewardOption.builder().missionSetId(missionSet.getId()).name("꽝").total(200).build(),
                RewardOption.builder().missionSetId(missionSet.getId()).name("텀블러").total(100).build(),
                RewardOption.builder().missionSetId(missionSet.getId()).name("에코백").total(50).build(),
                RewardOption.builder().missionSetId(missionSet.getId()).name("스티커팩").total(200).build()
        );
        rewardOptionRepository.saveAll(options);

        log.info("생성된 MissionSet ID: {}", missionSet.getId());
        log.info("미션/리워드 더미 데이터 생성 완료");
    }
}