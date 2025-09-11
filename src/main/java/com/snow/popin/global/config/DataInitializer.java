package com.snow.popin.global.config;

import com.snow.popin.domain.auth.constant.AuthProvider;
import com.snow.popin.domain.mission.entity.Mission;
import com.snow.popin.domain.mission.entity.MissionSet;
import com.snow.popin.domain.mission.repository.MissionRepository;
import com.snow.popin.domain.mission.repository.MissionSetRepository;
import com.snow.popin.domain.mypage.host.dto.PopupRegisterRequestDto;
import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.mypage.host.entity.Host;
import com.snow.popin.domain.mypage.host.entity.HostRole;
import com.snow.popin.domain.mypage.host.repository.BrandRepository;
import com.snow.popin.domain.mypage.host.repository.HostRepository;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.popupReservation.entity.Reservation;
import com.snow.popin.domain.popupReservation.repository.ReservationRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final PopupRepository popupRepository;
    private final BrandRepository brandRepository;
    private final HostRepository hostRepository; // HOST == BRADND_MEMBERS 테이블
    private final ReservationRepository reservationRepository;

    @Override
    public void run(String... args) throws Exception {
        createDummyUsers();
        createDummyMissions();
        createFakeHostAndReservations();
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

        log.info("팝업 + 미션셋 및 미션 더미 데이터를 생성합니다.");


        // 팝업 생성 (엔티티 수정 없이 생성 메서드 활용)
        Popup popup = Popup.createForTest("테스트 팝업", PopupStatus.ONGOING, null);
        // mainImageUrl은 세터가 없으니 리플렉션 없이 기본값으로 두거나 생성자쪽에서 넣도록 createForTest를 확장
        popupRepository.saveAndFlush(popup);

        // 미션셋 생성 (Popup ID 연결)
        MissionSet missionSet = MissionSet.builder()
                .popupId(popup.getId()) // Popup의 FK 연결
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
                RewardOption.builder().missionSetId(missionSet.getId()).name("엽서").total(200).build(),
                RewardOption.builder().missionSetId(missionSet.getId()).name("텀블러").total(100).build(),
                RewardOption.builder().missionSetId(missionSet.getId()).name("에코백").total(50).build(),
                RewardOption.builder().missionSetId(missionSet.getId()).name("스티커팩").total(200).build()
        );
        rewardOptionRepository.saveAll(options);

        log.info("생성된 Popup ID: {}", popup.getId());
        log.info("생성된 MissionSet ID: {}", missionSet.getId());
        log.info("미션/리워드 더미 데이터 생성 완료");
    }

    // 더미 브랜드, 호스트,팝업, 예약
    // 팝업 DTO 생성 유틸
    private PopupRegisterRequestDto buildPopupDto(String title) {
        PopupRegisterRequestDto dto = new PopupRegisterRequestDto();
        dto.setTitle(title);
        dto.setSummary("테스트 요약");
        dto.setDescription("테스트 설명");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(7));
        dto.setEntryFee(1);
        dto.setReservationAvailable(true);
        dto.setWaitlistAvailable(false);
        dto.setNotice("공지 없음");
        dto.setMainImageUrl("https://dummyimage.com/600x400");
        dto.setIsFeatured(false);
        return dto;
    }

    private void createFakeHostAndReservations() {
        // host1 유저 생성
        User host1 = userRepository.findByEmail("host1@test.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("host1@test.com")
                        .password(passwordEncoder.encode("1234"))
                        .name("호스트1")
                        .nickname("host1")
                        .phone("010-1111-1111")
                        .authProvider(AuthProvider.LOCAL)
                        .role(Role.HOST)
                        .build()));

        // reservation1 유저 생성
        User reservation1 = userRepository.findByEmail("reservation1@test.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("reservation1@test.com")
                        .password(passwordEncoder.encode("1234"))
                        .name("예약자1")
                        .nickname("reservation1")
                        .phone("010-2222-2222")
                        .authProvider(AuthProvider.LOCAL)
                        .role(Role.USER)
                        .build()));

        // 브랜드 생성
        Brand brand = brandRepository.findAll().stream()
                .filter(b -> b.getName().equals("가짜브랜드"))
                .findFirst()
                .orElseGet(() -> brandRepository.save(
                        Brand.builder()
                                .name("가짜브랜드")
                                .description("host1 전용 가짜 브랜드")
                                .businessType(Brand.BusinessType.INDIVIDUAL)
                                .build()
                ));

        // 호스트 등록 (userId로 체크)
        if (!hostRepository.existsByBrandAndUser(brand, host1.getId())) {
            hostRepository.save(Host.builder()
                    .brand(brand)
                    .user(host1)
                    .roleInBrand(HostRole.OWNER)
                    .build());
        }

        // 팝업 3개 생성 (중복 방지: 같은 이름 있으면 skip)
        if (popupRepository.findFirstByTitle("가짜 팝업1").isEmpty()) {
            Popup popup1 = Popup.create(brand.getId(), buildPopupDto("가짜 팝업1"));
            popupRepository.save(popup1);

            Reservation r1 = Reservation.create(
                    popup1, reservation1, "예약자1", "010-3333-3333", LocalDateTime.now().plusDays(1)
            );
            reservationRepository.save(r1);
        }

        if (popupRepository.findFirstByTitle("가짜 팝업2").isEmpty()) {
            Popup popup2 = Popup.create(brand.getId(), buildPopupDto("가짜 팝업2"));
            popupRepository.save(popup2);

            Reservation r2 = Reservation.create(
                    popup2, reservation1, "예약자1", "010-3333-3333", LocalDateTime.now().minusDays(1)
            );
            r2.markAsVisited();
            reservationRepository.save(r2);
        }

        if (popupRepository.findFirstByTitle("가짜 팝업3").isEmpty()) {
            Popup popup3 = Popup.create(brand.getId(), buildPopupDto("가짜 팝업3"));
            popupRepository.save(popup3);

            Reservation r3 = Reservation.create(
                    popup3, reservation1, "예약자1", "010-3333-3333", LocalDateTime.now().plusDays(2)
            );
            r3.cancel();
            reservationRepository.save(r3);
        }

        log.info(" 더미 데이터 생성 완료: host1@test.com → 브랜드/팝업3개, reservation1@test.com → 예약 3건");
    }



}