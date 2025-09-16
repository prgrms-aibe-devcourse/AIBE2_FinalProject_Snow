package com.snow.popin.global.config;

import com.snow.popin.domain.auth.constant.AuthProvider;
import com.snow.popin.domain.inquiry.entity.Inquiry;
import com.snow.popin.domain.inquiry.entity.InquiryStatus;
import com.snow.popin.domain.inquiry.entity.TargetType;
import com.snow.popin.domain.inquiry.repository.InquiryRepository;
import com.snow.popin.domain.map.entity.Venue;
import com.snow.popin.domain.map.repository.MapRepository;
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
import com.snow.popin.domain.roleupgrade.entity.RoleUpgrade;
import com.snow.popin.domain.roleupgrade.repository.RoleUpgradeRepository;
import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.space.repository.SpaceRepository;
import com.snow.popin.domain.user.repository.UserRepository;
import com.snow.popin.domain.user.constant.Role;
import com.snow.popin.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
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
    private final InquiryRepository inquiryRepository;
    private final RoleUpgradeRepository roleUpgradeRepository;
    private final SpaceRepository spaceRepository;
    private final MapRepository venueRepository;

    @Override
    public void run(String... args) throws Exception {
        createDummyUsers();
        createDummyVenues();
        createDummyMissions();
        createFakeHostAndReservations();
        createDummyInquiries();
        createDummyRoleUpgrades();
        createDummySpaces();
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

        User user3 = User.builder()
                .email("user3@test.com")
                .password(passwordEncoder.encode("1234"))
                .name("관리자1")
                .nickname("관리자1닉네임")
                .phone("010-5678-5678")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.ADMIN)
                .build();

        userRepository.saveAll(Arrays.asList(user1, user2, user3));
        log.info("유저 더미 데이터 생성 완료");
    }

    private void createDummyVenues() {
        if (venueRepository.count() > 0) {
            log.info("Venue 더미 데이터가 이미 존재하여 생성하지 않습니다.");
            return;
        }

        log.info("Venue 더미 데이터를 생성합니다.");

        Venue venue1 = Venue.of(
                "테스트 팝업 장소1",
                "서울특별시 강남구 테헤란로 123",
                "서울특별시 강남구 역삼동 123-45",
                "1층 101호",
                37.5012,
                127.0396,
                true
        );

        Venue venue2 = Venue.of(
                "테스트 팝업 장소2",
                "서울특별시 마포구 홍익로 234",
                "서울특별시 마포구 홍익동 234-56",
                "2층 전체",
                37.5512,
                126.9222,
                false
        );

        Venue venue3 = Venue.of(
                "테스트 팝업 장소3",
                "서울특별시 용산구 이태원로 345",
                "서울특별시 용산구 이태원동 345-67",
                "지하 1층",
                37.5345,
                126.9947,
                true
        );

        venueRepository.saveAll(Arrays.asList(venue1, venue2, venue3));
        log.info("Venue 더미 데이터 생성 완료");
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

    // BrandId 설정 헬퍼 메서드 (리플렉션 사용)
    private void setBrandId(Popup popup, Long brandId) {
        try {
            Field brandIdField = Popup.class.getDeclaredField("brandId");
            brandIdField.setAccessible(true);
            brandIdField.set(popup, brandId);
        } catch (Exception e) {
            log.warn("brandId 설정 실패: {}", e.getMessage());
        }
    }

    private void createDummyInquiries() {
        if (inquiryRepository.count() > 0) {
            log.info("신고 더미 데이터가 이미 존재하여 생성하지 않습니다.");
            return;
        }

        log.info("신고 더미 데이터를 생성합니다.");

        // 사용자들 가져오기
        User user1 = userRepository.findByEmail("user1@test.com").orElse(null);
        User user2 = userRepository.findByEmail("user2@test.com").orElse(null);
        User reservationUser = userRepository.findByEmail("reservation1@test.com").orElse(null);

        // 팝업들 가져오기
        List<Popup> popups = popupRepository.findAll();
        Long popup1Id = popups.size() > 0 ? popups.get(0).getId() : 1L;
        Long popup2Id = popups.size() > 1 ? popups.get(1).getId() : 1L;
        Long popup3Id = popups.size() > 2 ? popups.get(2).getId() : 1L;

        List<Inquiry> inquiries = Arrays.asList(
                // 팝업 신고 (4개)
                Inquiry.builder()
                        .email(user1 != null ? user1.getEmail() : "user1@test.com")
                        .targetType(TargetType.POPUP)
                        .targetId(popup1Id)
                        .subject("부적절한 콘텐츠 신고")
                        .content("이 팝업에서 부적절한 내용이 전시되고 있습니다. 미성년자에게 유해할 수 있는 콘텐츠가 포함되어 있어 신고합니다.")
                        .status(InquiryStatus.OPEN)
                        .build(),

                Inquiry.builder()
                        .email(user2 != null ? user2.getEmail() : "user2@test.com")
                        .targetType(TargetType.POPUP)
                        .targetId(popup2Id)
                        .subject("허위 정보 신고")
                        .content("팝업 정보가 실제와 다릅니다. 운영 시간과 위치가 잘못 기재되어 있어 많은 사람들이 피해를 보고 있습니다.")
                        .status(InquiryStatus.IN_PROGRESS)
                        .build(),

                Inquiry.builder()
                        .email(reservationUser != null ? reservationUser.getEmail() : "reservation1@test.com")
                        .targetType(TargetType.POPUP)
                        .targetId(popup3Id)
                        .subject("스팸 팝업 신고")
                        .content("동일한 팝업이 여러 번 중복 등록되어 있어 스팸으로 의심됩니다. 확인 부탁드립니다.")
                        .status(InquiryStatus.CLOSED)
                        .build(),

                Inquiry.builder()
                        .email("guest@test.com")
                        .targetType(TargetType.POPUP)
                        .targetId(popup1Id)
                        .subject("저작권 위반 신고")
                        .content("이 팝업에서 무단으로 타인의 저작물을 사용하고 있는 것 같습니다. 저작권 확인이 필요합니다.")
                        .status(InquiryStatus.OPEN)
                        .build(),

                // 장소 대여 신고 (3개)
                Inquiry.builder()
                        .email(user1 != null ? user1.getEmail() : "user1@test.com")
                        .targetType(TargetType.SPACE)
                        .targetId(1L) // 임시 공간 ID
                        .subject("시설 문제 신고")
                        .content("대여한 공간의 시설이 사진과 너무 다릅니다. 에어컨이 고장나고 화장실 상태가 매우 불량합니다.")
                        .status(InquiryStatus.OPEN)
                        .build(),

                Inquiry.builder()
                        .email(user2 != null ? user2.getEmail() : "user2@test.com")
                        .targetType(TargetType.SPACE)
                        .targetId(2L) // 임시 공간 ID
                        .subject("예약 취소 문제")
                        .content("예약 확정 후 일방적으로 취소당했습니다. 이미 행사 준비를 다 마쳤는데 너무 무책임한 것 같습니다.")
                        .status(InquiryStatus.IN_PROGRESS)
                        .build(),

                Inquiry.builder()
                        .email("renter@test.com")
                        .targetType(TargetType.SPACE)
                        .targetId(3L) // 임시 공간 ID
                        .subject("추가 비용 문제")
                        .content("계약서에 없던 추가 비용을 요구받았습니다. 청소비, 보증금 등 사전에 고지하지 않은 비용들입니다.")
                        .status(InquiryStatus.CLOSED)
                        .build(),

                // 리뷰 신고 (3개)
                Inquiry.builder()
                        .email(user1 != null ? user1.getEmail() : "user1@test.com")
                        .targetType(TargetType.REVIEW)
                        .targetId(1L) // 임시 리뷰 ID
                        .subject("욕설 및 비방 리뷰")
                        .content("이 리뷰에 욕설과 인신공격성 발언이 포함되어 있습니다. 다른 이용자들에게 불쾌감을 주는 내용입니다.")
                        .status(InquiryStatus.OPEN)
                        .build(),

                Inquiry.builder()
                        .email(reservationUser != null ? reservationUser.getEmail() : "reservation1@test.com")
                        .targetType(TargetType.REVIEW)
                        .targetId(2L) // 임시 리뷰 ID
                        .subject("허위 리뷰 신고")
                        .content("실제 방문하지 않고 작성한 것으로 보이는 허위 리뷰입니다. 내용이 너무 부정확하고 의도적인 것 같습니다.")
                        .status(InquiryStatus.IN_PROGRESS)
                        .build(),

                Inquiry.builder()
                        .email("reviewer@test.com")
                        .targetType(TargetType.REVIEW)
                        .targetId(3L) // 임시 리뷰 ID
                        .subject("스팸 리뷰 신고")
                        .content("같은 사용자가 동일한 내용의 리뷰를 여러 번 작성했습니다. 스팸성 리뷰로 판단됩니다.")
                        .status(InquiryStatus.CLOSED)
                        .build(),

                // 일반 문의 (4개) - targetId는 null
                Inquiry.builder()
                        .email(user2 != null ? user2.getEmail() : "user2@test.com")
                        .targetType(TargetType.GENERAL)
                        .targetId(1L) // GENERAL도 targetId 필요
                        .subject("로그인 문제 문의")
                        .content("앱에서 로그인이 자꾸 풀립니다. 계정 문제인지 확인해주세요. 아이폰 15 pro 사용 중입니다.")
                        .status(InquiryStatus.OPEN)
                        .build(),

                Inquiry.builder()
                        .email("helper@test.com")
                        .targetType(TargetType.GENERAL)
                        .targetId(2L)
                        .subject("즐겨찾기 기능 요청")
                        .content("팝업 즐겨찾기 기능을 추가해주시면 좋겠습니다. 관심있는 팝업들을 따로 모아서 볼 수 있으면 편할 것 같아요.")
                        .status(InquiryStatus.IN_PROGRESS)
                        .build(),

                Inquiry.builder()
                        .email(user1 != null ? user1.getEmail() : "user1@test.com")
                        .targetType(TargetType.GENERAL)
                        .targetId(3L)
                        .subject("결제 오류 신고")
                        .content("결제 과정에서 오류가 발생합니다. '결제 완료' 메시지는 뜨는데 실제로는 결제가 안 되어 있어요. 확인 부탁드립니다.")
                        .status(InquiryStatus.CLOSED)
                        .build(),

                Inquiry.builder()
                        .email("feedback@test.com")
                        .targetType(TargetType.GENERAL)
                        .targetId(4L)
                        .subject("검색 기능 개선 요청")
                        .content("검색 기능이 좀 더 정확했으면 좋겠습니다. 키워드로 검색할 때 관련성이 떨어지는 결과들이 많이 나와요.")
                        .status(InquiryStatus.OPEN)
                        .build(),

                // 사용자 신고 (2개)
                Inquiry.builder()
                        .email(reservationUser != null ? reservationUser.getEmail() : "reservation1@test.com")
                        .targetType(TargetType.USER)
                        .targetId(user1 != null ? user1.getId() : 1L)
                        .subject("사용자 부적절 행위 신고")
                        .content("이 사용자가 팝업장에서 다른 방문객들에게 불쾌감을 주는 행위를 했습니다. 사진 촬영을 방해하고 시비를 걸었어요.")
                        .status(InquiryStatus.OPEN)
                        .build(),

                Inquiry.builder()
                        .email("reporter@test.com")
                        .targetType(TargetType.USER)
                        .targetId(user2 != null ? user2.getId() : 2L)
                        .subject("스팸 사용자 신고")
                        .content("이 사용자가 계속해서 스팸성 리뷰와 댓글을 작성하고 있습니다. 광고성 내용들이 대부분이에요.")
                        .status(InquiryStatus.IN_PROGRESS)
                        .build()
        );

        inquiryRepository.saveAll(inquiries);
        log.info("신고 더미 데이터 생성 완료 - 총 {}건", inquiries.size());
    }

    private void createDummyRoleUpgrades() {
        if (roleUpgradeRepository.count() > 0) {
            log.info("역할 승격 요청 더미 데이터가 이미 존재하여 생성하지 않습니다.");
            return;
        }

        log.info("역할 승격 요청 더미 데이터를 생성합니다.");

        List<RoleUpgrade> roleUpgrades = Arrays.asList(
                // HOST 요청 (3개)
                RoleUpgrade.builder()
                        .email("host-applicant1@test.com")
                        .requestedRole(Role.HOST)
                        .payload("{\"companyName\":\"팝업스토어 전문업체\",\"businessNumber\":\"123-45-67890\",\"description\":\"다양한 브랜드와 협업하여 팝업스토어를 기획하고 운영하는 전문업체입니다. 3년간의 운영 경험을 보유하고 있습니다.\"}")
                        .build(),

                RoleUpgrade.builder()
                        .email("host-applicant2@test.com")
                        .requestedRole(Role.HOST)
                        .payload("{\"companyName\":\"크리에이티브 스페이스\",\"businessNumber\":\"987-65-43210\",\"description\":\"창의적인 공간 기획과 이벤트 운영을 전문으로 하는 업체입니다. 아티스트와의 협업 경험이 풍부합니다.\"}")
                        .build(),

                RoleUpgrade.builder()
                        .email("host-applicant3@test.com")
                        .requestedRole(Role.HOST)
                        .payload("{\"companyName\":\"문화공간 기획\",\"businessNumber\":\"456-78-91230\",\"description\":\"문화예술 분야의 팝업 이벤트를 전문적으로 기획하고 운영합니다. 지역 커뮤니티와의 연계 프로그램을 중점적으로 진행합니다.\"}")
                        .build(),

                // PROVIDER 요청 (3개)
                RoleUpgrade.builder()
                        .email("provider-applicant1@test.com")
                        .requestedRole(Role.PROVIDER)
                        .payload("{\"companyName\":\"프리미엄 공간 대여\",\"businessNumber\":\"111-22-33444\",\"businessType\":\"CORPORATION\",\"description\":\"강남, 홍대, 이태원 등 핫플레이스에 위치한 프리미엄 공간을 보유하고 있습니다. 총 15개의 다양한 컨셉의 공간을 운영 중입니다.\",\"spaceCount\":15}")
                        .build(),

                RoleUpgrade.builder()
                        .email("provider-applicant2@test.com")
                        .requestedRole(Role.PROVIDER)
                        .payload("{\"companyName\":\"유니크 베뉴\",\"businessNumber\":\"555-66-77888\",\"businessType\":\"INDIVIDUAL\",\"description\":\"독특하고 개성있는 소규모 공간을 제공합니다. 사진 촬영과 소규모 전시에 최적화된 공간들로 구성되어 있습니다.\",\"spaceCount\":5}")
                        .build(),

                RoleUpgrade.builder()
                        .email("provider-applicant3@test.com")
                        .requestedRole(Role.PROVIDER)
                        .payload("{\"companyName\":\"뉴트로 스페이스\",\"businessNumber\":\"999-88-77666\",\"businessType\":\"CORPORATION\",\"description\":\"레트로와 모던함이 조화된 뉴트로 컨셉의 공간을 다수 보유하고 있습니다. MZ세대 타겟의 팝업에 최적화된 공간들입니다.\",\"spaceCount\":8}")
                        .build()
        );

        // 상태 다양하게 설정
        roleUpgrades.get(0).approve(); // 첫 번째는 승인됨
        roleUpgrades.get(1).reject("제출된 서류에 누락이 있습니다. 사업자등록증과 공간 관련 서류를 추가로 제출해주세요."); // 두 번째는 반려됨
        // 나머지는 대기중 상태 유지 (기본값)

        roleUpgradeRepository.saveAll(roleUpgrades);
        log.info("역할 승격 요청 더미 데이터 생성 완료 - 총 {}건", roleUpgrades.size());
    }

    /**
     * 장소 더미 데이터 생성
     */
    private void createDummySpaces() {
        if (spaceRepository.count() > 0) {
            log.info("장소 더미 데이터가 이미 존재하여 생성하지 않습니다.");
            return;
        }

        log.info("장소 더미 데이터를 생성합니다.");

        // PROVIDER 역할의 사용자들 생성
        User provider1 = userRepository.findByEmail("provider1@test.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("provider1@test.com")
                        .password(passwordEncoder.encode("1234"))
                        .name("공간제공자1")
                        .nickname("provider1")
                        .phone("010-1111-2222")
                        .authProvider(AuthProvider.LOCAL)
                        .role(Role.PROVIDER)
                        .build()));

        User provider2 = userRepository.findByEmail("provider2@test.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("provider2@test.com")
                        .password(passwordEncoder.encode("1234"))
                        .name("공간제공자2")
                        .nickname("provider2")
                        .phone("010-2222-3333")
                        .authProvider(AuthProvider.LOCAL)
                        .role(Role.PROVIDER)
                        .build()));

        // 일반 사용자도 공간 등록 가능
        User user1 = userRepository.findByEmail("user1@test.com").orElse(null);

        // 추가 Venue 데이터 생성 (기존에 없다면)
        List<Venue> venues = venueRepository.findAll();

        if (venues.size() < 5) {
            Venue venue4 = Venue.of(
                    "송파 컨벤션센터",
                    "서울특별시 송파구 올림픽로 456",
                    "서울특별시 송파구 잠실동 456-78",
                    "메인홀",
                    37.5145,
                    127.1026,
                    true
            );

            Venue venue5 = Venue.of(
                    "성수동 카페 스페이스",
                    "서울특별시 성동구 성수일로 567",
                    "서울특별시 성동구 성수동2가 567-89",
                    "1층 카페공간",
                    37.5445,
                    127.0560,
                    false
            );

            venueRepository.saveAll(Arrays.asList(venue4, venue5));
            venues = venueRepository.findAll(); // 다시 로드
        }

        // Space 데이터 생성 (기존 빌더 패턴 사용)
        List<Space> spaces = Arrays.asList(
                Space.builder()
                        .owner(provider1)
                        .title("강남 프리미엄 스튜디오")
                        .description("촬영, 전시회, 세미나에 최적화된 프리미엄 스튜디오입니다. 최신 조명 장비와 음향 시설을 완비하고 있으며, 강남역에서 도보 5분 거리에 위치해 있어 접근성이 뛰어납니다.")
                        .areaSize(120)
                        .startDate(LocalDate.now().minusDays(10))
                        .endDate(LocalDate.now().plusDays(60))
                        .rentalFee(300000)
                        .contactPhone("010-1111-2222")
                        .coverImageUrl("https://dummyimage.com/600x400/4A90E2/ffffff?text=Premium+Studio")
                        .venue(venues.get(0))
                        .build(),

                Space.builder()
                        .owner(provider1)
                        .title("홍대 아트스페이스")
                        .description("예술 작품 전시와 창작 활동을 위한 아트스페이스입니다. 자연광이 풍부하게 들어오는 넓은 공간으로, 갤러리나 워크샵 진행에 적합합니다.")
                        .areaSize(80)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(90))
                        .rentalFee(180000)
                        .contactPhone("010-1111-2222")
                        .coverImageUrl("https://dummyimage.com/600x400/E74C3C/ffffff?text=Art+Space")
                        .venue(venues.size() > 1 ? venues.get(1) : venues.get(0))
                        .build(),

                Space.builder()
                        .owner(provider2)
                        .title("이태원 이벤트홀")
                        .description("각종 이벤트, 파티, 워크샵을 위한 다목적 이벤트홀입니다. 최대 100명까지 수용 가능하며, 케이터링 서비스도 이용 가능합니다.")
                        .areaSize(200)
                        .startDate(LocalDate.now().plusDays(5))
                        .endDate(LocalDate.now().plusDays(120))
                        .rentalFee(500000)
                        .contactPhone("010-2222-3333")
                        .coverImageUrl("https://dummyimage.com/600x400/F39C12/ffffff?text=Event+Hall")
                        .venue(venues.size() > 2 ? venues.get(2) : venues.get(0))
                        .build(),

                Space.builder()
                        .owner(provider2)
                        .title("송파 컨벤션센터")
                        .description("대규모 컨퍼런스, 전시회, 세미나를 위한 컨벤션센터입니다. 최신 AV 장비와 동시통역 시설을 갖추고 있어 국제 행사에도 적합합니다.")
                        .areaSize(500)
                        .startDate(LocalDate.now().minusDays(5))
                        .endDate(LocalDate.now().plusDays(180))
                        .rentalFee(1200000)
                        .contactPhone("010-2222-3333")
                        .coverImageUrl("https://dummyimage.com/600x400/9B59B6/ffffff?text=Convention+Center")
                        .venue(venues.size() > 3 ? venues.get(3) : venues.get(0))
                        .build(),

                Space.builder()
                        .owner(user1 != null ? user1 : provider1)
                        .title("성수동 카페 스페이스")
                        .description("아늑한 카페 분위기의 소규모 모임 공간입니다. 북카페 형태로 운영되며, 소규모 독서모임, 스터디, 브런치 파티 등에 적합합니다.")
                        .areaSize(50)
                        .startDate(LocalDate.now().plusDays(1))
                        .endDate(LocalDate.now().plusDays(45))
                        .rentalFee(80000)
                        .contactPhone("010-1234-1234")
                        .coverImageUrl("https://dummyimage.com/600x400/1ABC9C/ffffff?text=Cafe+Space")
                        .venue(venues.size() > 4 ? venues.get(4) : venues.get(0))
                        .build()
        );

        // 일부는 비공개로 설정 (리플렉션 사용)
        try {
            Field isPublicField = Space.class.getDeclaredField("isPublic");
            isPublicField.setAccessible(true);
            isPublicField.set(spaces.get(1), false); // 홍대 아트스페이스 비공개
            isPublicField.set(spaces.get(4), false); // 성수동 카페 스페이스 비공개
        } catch (Exception e) {
            log.warn("isPublic 필드 설정 실패: {}", e.getMessage());
        }

        spaceRepository.saveAll(spaces);

        log.info("장소 더미 데이터 생성 완료 - 총 {}개", spaces.size());
        log.info("공개 장소: {}개, 비공개 장소: {}개",
                spaces.stream().mapToInt(s -> s.getIsPublic() ? 1 : 0).sum(),
                spaces.stream().mapToInt(s -> s.getIsPublic() ? 0 : 1).sum());
    }
}