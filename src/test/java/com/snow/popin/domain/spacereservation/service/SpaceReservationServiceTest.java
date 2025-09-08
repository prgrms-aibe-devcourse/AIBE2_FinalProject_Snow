package com.snow.popin.domain.spacereservation.service;

import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.space.repository.SpaceRepository;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationCreateRequestDto;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationListResponseDto;
import com.snow.popin.domain.spacereservation.entity.SpaceReservation;
import com.snow.popin.domain.spacereservation.entity.ReservationStatus;
import com.snow.popin.domain.spacereservation.repository.SpaceReservationRepository;
import com.snow.popin.domain.user.constant.Role;
import com.snow.popin.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpaceReservationServiceTest {

    @Mock
    private SpaceReservationRepository reservationRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @InjectMocks
    private SpaceReservationService reservationService;

    private User hostUser;
    private User providerUser;
    private Space space;
    private SpaceReservationCreateRequestDto createDto;

    @BeforeEach
    void setUp() {
        // HOST 사용자
        hostUser = User.builder()
                .id(1L)
                .email("host@test.com")
                .name("HOST 사용자")
                .role(Role.HOST)
                .build();

        // PROVIDER 사용자 (공간 소유자)
        providerUser = User.builder()
                .id(2L)
                .email("provider@test.com")
                .name("PROVIDER 사용자")
                .role(Role.USER)
                .build();

        // 테스트용 공간
        space = Space.builder()
                .owner(providerUser)
                .title("테스트 공간")
                .address("서울시 강남구")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .rentalFee(1000000)
                .build();

        // 예약 생성 DTO
        createDto = new SpaceReservationCreateRequestDto();
        createDto.setSpaceId(1L);
        createDto.setBrand("테스트 브랜드");
        createDto.setPopupTitle("테스트 팝업");
        createDto.setStartDate(LocalDate.of(2024, 6, 1));
        createDto.setEndDate(LocalDate.of(2024, 6, 7));
        createDto.setMessage("테스트 예약 요청입니다.");
        createDto.setContactPhone("010-1234-5678");
        createDto.setPopupDescription("팝업 스토어 설명");
    }

    @Test
    @DisplayName("예약 생성 성공")
    void createReservation_Success() {
        // given
        given(spaceRepository.findById(1L)).willReturn(Optional.of(space));
        given(reservationRepository.countOverlappingReservations(any(), any(), any()))
                .willReturn(0L);

        SpaceReservation savedReservation = SpaceReservation.builder()
                .id(1L)
                .space(space)
                .host(hostUser)
                .brand("테스트 브랜드")
                .popupTitle("테스트 팝업")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 7))
                .status(ReservationStatus.PENDING)
                .build();

        given(reservationRepository.save(any(SpaceReservation.class)))
                .willReturn(savedReservation);

        // when
        Long reservationId = reservationService.createReservation(hostUser, createDto);

        // then
        assertThat(reservationId).isEqualTo(1L);
        verify(spaceRepository).findById(1L);
        verify(reservationRepository).save(any(SpaceReservation.class));
    }

    @Test
    @DisplayName("예약 생성 실패 - HOST 권한 없음")
    void createReservation_Fail_NotHost() {
        // given
        User normalUser = User.builder()
                .id(3L)
                .email("user@test.com")
                .role(Role.USER)
                .build();

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(normalUser, createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("HOST 권한이 필요합니다.");
    }

    @Test
    @DisplayName("예약 생성 실패 - 존재하지 않는 공간")
    void createReservation_Fail_SpaceNotFound() {
        // given
        given(spaceRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(hostUser, createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 공간입니다.");
    }

    @Test
    @DisplayName("예약 생성 실패 - 본인 공간 예약")
    void createReservation_Fail_OwnSpace() {
        // given
        Space ownSpace = Space.builder()
                .owner(hostUser) // HOST가 소유한 공간
                .title("내 공간")
                .build();

        given(spaceRepository.findById(1L)).willReturn(Optional.of(ownSpace));

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(hostUser, createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인 공간은 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("예약 생성 실패 - 이용 가능 기간 벗어남")
    void createReservation_Fail_DateOutOfRange() {
        // given
        createDto.setStartDate(LocalDate.of(2025, 1, 1)); // 공간 이용 기간을 벗어남
        createDto.setEndDate(LocalDate.of(2025, 1, 7));

        given(spaceRepository.findById(1L)).willReturn(Optional.of(space));

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(hostUser, createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("공간 이용 가능 기간을 벗어났습니다.");
    }

    @Test
    @DisplayName("예약 생성 실패 - 날짜 중복")
    void createReservation_Fail_DateOverlap() {
        // given
        given(spaceRepository.findById(1L)).willReturn(Optional.of(space));
        given(reservationRepository.countOverlappingReservations(any(), any(), any()))
                .willReturn(1L); // 중복된 예약 존재

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(hostUser, createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 기간에 이미 승인된 예약이 있습니다.");
    }

    @Test
    @DisplayName("내 예약 요청 목록 조회")
    void getMyRequests_Success() {
        // given
        SpaceReservation reservation1 = SpaceReservation.builder()
                .id(1L)
                .space(space)
                .host(hostUser)
                .brand("브랜드1")
                .popupTitle("팝업1")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 7))
                .status(ReservationStatus.PENDING)
                .build();

        SpaceReservation reservation2 = SpaceReservation.builder()
                .id(2L)
                .space(space)
                .host(hostUser)
                .brand("브랜드2")
                .popupTitle("팝업2")
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 7, 7))
                .status(ReservationStatus.ACCEPTED)
                .build();

        given(reservationRepository.findByHostOrderByCreatedAtDesc(hostUser))
                .willReturn(Arrays.asList(reservation1, reservation2));

        // when
        List<SpaceReservationListResponseDto> result = reservationService.getMyRequests(hostUser);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBrand()).isEqualTo("브랜드1");
        assertThat(result.get(0).getPopupTitle()).isEqualTo("팝업1");
        assertThat(result.get(1).getBrand()).isEqualTo("브랜드2");
        assertThat(result.get(1).getPopupTitle()).isEqualTo("팝업2");
    }

    @Test
    @DisplayName("내 공간 예약 목록 조회")
    void getMySpaceReservations_Success() {
        // given
        SpaceReservation reservation = SpaceReservation.builder()
                .id(1L)
                .space(space)
                .host(hostUser)
                .brand("테스트 브랜드")
                .popupTitle("테스트 팝업")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 7))
                .status(ReservationStatus.PENDING)
                .build();

        given(reservationRepository.findBySpaceOwnerOrderByCreatedAtDesc(providerUser))
                .willReturn(Arrays.asList(reservation));

        // when
        List<SpaceReservationListResponseDto> result =
                reservationService.getMySpaceReservations(providerUser);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrand()).isEqualTo("테스트 브랜드");
        assertThat(result.get(0).getPopupTitle()).isEqualTo("테스트 팝업");
        assertThat(result.get(0).getHostName()).isEqualTo("HOST 사용자");
    }

    @Test
    @DisplayName("예약 승인 성공")
    void acceptReservation_Success() {
        // given
        SpaceReservation reservation = SpaceReservation.builder()
                .id(1L)
                .space(space)
                .host(hostUser)
                .brand("테스트 브랜드")
                .popupTitle("테스트 팝업")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 7))
                .status(ReservationStatus.PENDING)
                .build();

        given(reservationRepository.findByIdAndSpaceOwner(1L, providerUser))
                .willReturn(Optional.of(reservation));
        given(reservationRepository.countOverlappingReservations(any(), any(), any()))
                .willReturn(0L);

        // when
        reservationService.acceptReservation(providerUser, 1L);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACCEPTED);
    }

    @Test
    @DisplayName("예약 거절 성공")
    void rejectReservation_Success() {
        // given
        SpaceReservation reservation = SpaceReservation.builder()
                .id(1L)
                .space(space)
                .host(hostUser)
                .brand("테스트 브랜드")
                .popupTitle("테스트 팝업")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 7))
                .status(ReservationStatus.PENDING)
                .build();

        given(reservationRepository.findByIdAndSpaceOwner(1L, providerUser))
                .willReturn(Optional.of(reservation));

        // when
        reservationService.rejectReservation(providerUser, 1L);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REJECTED);
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancelReservation_Success() {
        // given
        SpaceReservation reservation = SpaceReservation.builder()
                .id(1L)
                .space(space)
                .host(hostUser)
                .brand("테스트 브랜드")
                .popupTitle("테스트 팝업")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 7))
                .status(ReservationStatus.PENDING)
                .build();

        given(reservationRepository.findByIdAndHost(1L, hostUser))
                .willReturn(Optional.of(reservation));

        // when
        reservationService.cancelReservation(hostUser, 1L);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }
}