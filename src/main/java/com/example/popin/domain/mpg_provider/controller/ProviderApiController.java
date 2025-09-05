package com.example.popin.domain.mpg_provider.controller;

import com.example.popin.domain.space.entity.Space;
import com.example.popin.domain.mpg_provider.service.ProviderService;
import com.example.popin.domain.spacereservation.dto.SpaceReservationListResponseDto;
import com.example.popin.domain.spacereservation.dto.SpaceReservationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/provider")
public class ProviderApiController {

    private final ProviderService service;

    // 내 등록 공간 리스트 (마이페이지 카드용)
    @GetMapping("/spaces")
    public ResponseEntity<List<Space>> loadMySpaceInProfile(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(service.findMySpaces(email));
    }

    // 내 공간에 신청된 예약 목록
    @GetMapping("/reservations")
    public ResponseEntity<List<SpaceReservationListResponseDto>> getReservationsToMySpaces(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(service.getReservationsToMySpaces(email));
    }

    // 예약 상세 조회
    @GetMapping("/reservations/{id}")
    public ResponseEntity<SpaceReservationResponseDto> getReservationDetail(
            @PathVariable Long id, Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(service.getReservationDetail(email, id));
    }

    // 예약 승인
    @PutMapping("/reservations/{id}/accept")
    public ResponseEntity<Map<String, String>> accept_space_Reservation(
            @PathVariable Long id, Principal principal) {
        String email = principal.getName();
        service.acceptReservation(email, id);
        return ResponseEntity.ok(Map.of("message", "예약이 승인되었습니다."));
    }

    // 예약 거절
    @PutMapping("/reservations/{id}/reject")
    public ResponseEntity<Map<String, String>> reject_space_Reservation(
            @PathVariable Long id, Principal principal) {
        String email = principal.getName();
        service.rejectReservation(email, id);
        return ResponseEntity.ok(Map.of("message", "예약이 거절되었습니다."));
    }

    // 예약 통계 추가
}
