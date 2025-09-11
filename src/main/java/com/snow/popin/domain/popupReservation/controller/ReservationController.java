package com.snow.popin.domain.popupReservation.controller;

import com.snow.popin.domain.popupReservation.dto.ReservationRequestDto;
import com.snow.popin.domain.popupReservation.dto.ReservationResponseDto;
import com.snow.popin.domain.popupReservation.service.ReservationService;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;


/**
 * 예약 관련 REST 컨트롤러
 *
 * - 팝업 예약 생성
 * - 내 예약 목록 조회
 * - 예약 취소
 * - 팝업별 예약 현황 조회 (호스트용)
 * - 방문 완료 처리
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserUtil userUtil;
    /**
     * 팝업 예약 생성
     *
     * @param popupId 예약할 팝업 ID
     * @param dto 예약 요청 DTO
     */
    @PostMapping("/popups/{popupId}")
    public ResponseEntity<?> createReservation(
            @PathVariable Long popupId,
            @Valid @RequestBody ReservationRequestDto dto) {

        User currentUser = userUtil.getCurrentUser();
        Long reservationId = reservationService.createReservation(currentUser, popupId, dto);

        return ResponseEntity.ok(Map.of(
                "reservationId", reservationId,
                "message", "예약이 완료되었습니다."
        ));
    }
    /**
     * 현재 로그인 사용자의 예약 목록 조회
     *
     * @return 예약 응답 DTO 리스트
     */
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponseDto>> getMyReservations() {
        User currentUser = userUtil.getCurrentUser();
        return ResponseEntity.ok(reservationService.getMyReservations(currentUser));
    }
    /**
     * 예약 취소
     *
     * @param reservationId 취소할 예약 ID
     */
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId) {
        User currentUser = userUtil.getCurrentUser();
        reservationService.cancelReservation(reservationId,currentUser);

        return ResponseEntity.ok(Map.of("message", "예약이 취소되었습니다."));
    }
    /**
     * 특정 팝업에 대한 예약 현황 조회 (호스트 권한 필요)
     *
     * @param popupId 팝업 ID
     * @return 예약 응답 DTO 리스트
     */
    @GetMapping("/popups/{popupId}")
    public ResponseEntity<List<ReservationResponseDto>> getPopupReservations(@PathVariable Long popupId) {
        User currentUser = userUtil.getCurrentUser();
        return ResponseEntity.ok(reservationService.getPopupReservations(popupId, currentUser));
    }
    /**
     * 예약을 방문 완료로 처리
     *
     * @param reservationId 예약 ID
     * @return 방문 완료 메시지
     */
    @PutMapping("/{reservationId}/visit")
    public ResponseEntity<?> markAsVisited(@PathVariable Long reservationId) {
        User currentUser = userUtil.getCurrentUser();
        reservationService.markAsVisited(reservationId, currentUser);

        return ResponseEntity.ok(Map.of("message", "방문 완료로 처리되었습니다."));
    }
}
