package com.snow.popin.domain.spacereservation.controller;

import com.snow.popin.domain.spacereservation.dto.SpaceReservationCreateRequestDto;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationListResponseDto;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationResponseDto;
import com.snow.popin.domain.spacereservation.service.SpaceReservationService;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/space-reservations")
@Slf4j
public class SpaceReservationController {

    private final SpaceReservationService reservationService;
    private final UserRepository userRepository;

    /** 예약 요청 생성 */
    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody SpaceReservationCreateRequestDto dto) {
        try {
            User host = getCurrentUser();
            Long reservationId = reservationService.createReservation(host, dto);
            return ResponseEntity.ok(Map.of("id", reservationId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 내가 신청한 예약 목록 (HOST) */
    @GetMapping("/my-requests")
    public List<SpaceReservationListResponseDto> getMyRequests() {
        return reservationService.getMyRequests(getCurrentUser());
    }

    /** 내 공간에 신청된 예약 목록 (PROVIDER) */
    @GetMapping("/my-spaces")
    public List<SpaceReservationListResponseDto> getMySpaceReservations() {
        return reservationService.getMySpaceReservations(getCurrentUser());
    }

    /** 예약 상세 조회 */
    @GetMapping("/{id}")
    public SpaceReservationResponseDto getReservationDetail(@PathVariable Long id) {
        return reservationService.getReservationDetail(getCurrentUser(), id);
    }

    /** 예약 승인 (PROVIDER) */
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptReservation(@PathVariable Long id) {
        try {
            reservationService.acceptReservation(getCurrentUser(), id);
            return ResponseEntity.ok(Map.of("message", "예약이 승인되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 예약 거절 (PROVIDER) */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectReservation(@PathVariable Long id) {
        try {
            reservationService.rejectReservation(getCurrentUser(), id);
            return ResponseEntity.ok(Map.of("message", "예약이 거절되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 예약 취소 (HOST) */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            reservationService.cancelReservation(getCurrentUser(), id);
            return ResponseEntity.ok(Map.of("message", "예약이 취소되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 예약 삭제 (거절된 예약만, PROVIDER) */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteReservation(@PathVariable Long id) {
        try {
            reservationService.deleteReservation(getCurrentUser(), id);
            return ResponseEntity.ok(Map.of("message", "예약이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 현재 로그인한 User 조회 */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "테스트용 사용자가 없습니다"));
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."));
    }
}
