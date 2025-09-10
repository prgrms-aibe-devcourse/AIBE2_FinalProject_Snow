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

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserUtil userUtil;

    // 팝업 예약하기
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

    // 내 예약 목록 조회
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponseDto>> getMyReservations() {
        User currentUser = userUtil.getCurrentUser();
        return ResponseEntity.ok(reservationService.getMyReservations(currentUser));
    }

    // 예약 취소
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId) {
        User currentUser = userUtil.getCurrentUser();
        reservationService.cancelReservation(reservationId,currentUser);

        return ResponseEntity.ok(Map.of("message", "예약이 취소되었습니다."));
    }

    // 팝업별 예약 현황 조회 (호스트용)
    @GetMapping("/popups/{popupId}")
    public ResponseEntity<List<ReservationResponseDto>> getPopupReservations(@PathVariable Long popupId) {
        User currentUser = userUtil.getCurrentUser();
        return ResponseEntity.ok(reservationService.getPopupReservations(popupId, currentUser));
    }

    //  방문 완료 처리
    @PutMapping("/{reservationId}/visit")
    public ResponseEntity<?> markAsVisited(@PathVariable Long reservationId) {
        User currentUser = userUtil.getCurrentUser();
        reservationService.markAsVisited(reservationId, currentUser);

        return ResponseEntity.ok(Map.of("message", "방문 완료로 처리되었습니다."));
    }
}
