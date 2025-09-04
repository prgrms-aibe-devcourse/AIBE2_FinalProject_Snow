package com.example.popin.domain.spacereservation.controller;

import com.example.popin.domain.spacereservation.dto.SpaceReservationCreateRequestDto;
import com.example.popin.domain.spacereservation.dto.SpaceReservationListResponseDto;
import com.example.popin.domain.spacereservation.dto.SpaceReservationResponseDto;
import com.example.popin.domain.spacereservation.entity.ReservationStatus;
import com.example.popin.domain.spacereservation.service.SpaceReservationService;
import com.example.popin.domain.user.constant.Role;
import com.example.popin.domain.user.entity.User;
import com.example.popin.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleSpaceReservationControllerTest {

    @InjectMocks
    private SpaceReservationController controller;

    @Mock
    private SpaceReservationService reservationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BindingResult bindingResult;

    private User hostUser;
    private User providerUser;
    private SpaceReservationCreateRequestDto createRequestDto;

    @BeforeEach
    void setUp() {
        hostUser = User.builder()
                .id(1L)
                .email("host@test.com")
                .name("HOST 사용자")
                .role(Role.HOST)
                .build();

        providerUser = User.builder()
                .id(2L)
                .email("provider@test.com")
                .name("공간 제공자")
                .role(Role.USER)
                .build();

        createRequestDto = new SpaceReservationCreateRequestDto();
        createRequestDto.setSpaceId(1L);
        createRequestDto.setBrand("테스트 브랜드");
        createRequestDto.setPopupTitle("테스트 팝업");
        createRequestDto.setStartDate(LocalDate.of(2024, 6, 1));
        createRequestDto.setEndDate(LocalDate.of(2024, 6, 7));
        createRequestDto.setMessage("테스트 예약 요청");
        createRequestDto.setContactPhone("010-1234-5678");
        createRequestDto.setPopupDescription("팝업 설명");
    }

    @Test
    @DisplayName("예약 생성 성공 - 단순 메소드 호출 테스트")
    void createReservation_Success() {
        // given
        given(bindingResult.hasErrors()).willReturn(false);
        given(userRepository.findAll()).willReturn(Arrays.asList(hostUser));
        given(reservationService.createReservation(any(User.class), any(SpaceReservationCreateRequestDto.class)))
                .willReturn(1L);

        // when
        ResponseEntity<?> response = controller.createReservation(createRequestDto);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody.get("id")).isEqualTo(1L);

        verify(reservationService).createReservation(any(User.class), eq(createRequestDto));
    }

    @Test
    @DisplayName("예약 생성 실패 - 유효성 검증 오류")
    void createReservation_ValidationFail() {
        // given
        given(bindingResult.hasErrors()).willReturn(true);

        // when
        ResponseEntity<?> response = controller.createReservation(createRequestDto);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        verify(reservationService, never()).createReservation(any(), any());
    }

    @Test
    @DisplayName("예약 생성 실패 - 비즈니스 로직 오류")
    void createReservation_BusinessLogicFail() {
        // given
        given(bindingResult.hasErrors()).willReturn(false);
        given(userRepository.findAll()).willReturn(Arrays.asList(hostUser));
        given(reservationService.createReservation(any(User.class), any(SpaceReservationCreateRequestDto.class)))
                .willThrow(new IllegalArgumentException("본인 공간은 예약할 수 없습니다."));

        // when
        ResponseEntity<?> response = controller.createReservation(createRequestDto);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertThat(responseBody.get("error")).isEqualTo("본인 공간은 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("내 예약 요청 목록 조회 성공")
    void getMyRequests_Success() {
        // given
        SpaceReservationListResponseDto dto = SpaceReservationListResponseDto.builder()
                .id(1L)
                .brand("테스트 브랜드")
                .popupTitle("테스트 팝업")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 7))
                .status(ReservationStatus.PENDING)
                .spaceTitle("테스트 공간")
                .build();

        given(userRepository.findAll()).willReturn(Arrays.asList(hostUser));
        given(reservationService.getMyRequests(any(User.class))).willReturn(Arrays.asList(dto));

        // when
        List<SpaceReservationListResponseDto> result = controller.getMyRequests();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrand()).isEqualTo("테스트 브랜드");
        assertThat(result.get(0).getPopupTitle()).isEqualTo("테스트 팝업");
        verify(reservationService).getMyRequests(any(User.class));
    }

    @Test
    @DisplayName("예약 승인 성공")
    void acceptReservation_Success() {
        // given
        given(userRepository.findAll()).willReturn(Arrays.asList(providerUser));
        doNothing().when(reservationService).acceptReservation(any(User.class), eq(1L));

        // when
        ResponseEntity<?> response = controller.acceptReservation(1L);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertThat(responseBody.get("message")).isEqualTo("예약이 승인되었습니다.");

        verify(reservationService).acceptReservation(any(User.class), eq(1L));
    }

    @Test
    @DisplayName("예약 승인 실패 - 권한 없음")
    void acceptReservation_Fail() {
        // given
        given(userRepository.findAll()).willReturn(Arrays.asList(hostUser));
        doThrow(new IllegalArgumentException("예약이 존재하지 않거나 승인 권한이 없습니다."))
                .when(reservationService).acceptReservation(any(User.class), eq(1L));

        // when
        ResponseEntity<?> response = controller.acceptReservation(1L);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertThat(responseBody.get("error")).isEqualTo("예약이 존재하지 않거나 승인 권한이 없습니다.");
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancelReservation_Success() {
        // given
        given(userRepository.findAll()).willReturn(Arrays.asList(hostUser));
        doNothing().when(reservationService).cancelReservation(any(User.class), eq(1L));

        // when
        ResponseEntity<?> response = controller.cancelReservation(1L);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertThat(responseBody.get("message")).isEqualTo("예약이 취소되었습니다.");

        verify(reservationService).cancelReservation(any(User.class), eq(1L));
    }

    @Test
    @DisplayName("예약 취소 실패 - 권한 없음")
    void cancelReservation_Fail() {
        // given
        given(userRepository.findAll()).willReturn(Arrays.asList(hostUser));
        doThrow(new IllegalArgumentException("예약이 존재하지 않거나 취소 권한이 없습니다."))
                .when(reservationService).cancelReservation(any(User.class), eq(1L));

        // when
        ResponseEntity<?> response = controller.cancelReservation(1L);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertThat(responseBody.get("error")).isEqualTo("예약이 존재하지 않거나 취소 권한이 없습니다.");
    }
}