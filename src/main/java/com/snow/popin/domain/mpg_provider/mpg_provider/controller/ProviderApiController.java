package com.snow.popin.domain.mpg_provider.mpg_provider.controller;

import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.mpg_provider.mpg_provider.service.ProviderService;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationListResponseDto;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationResponseDto;
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

    // 내 등록 공간 리스트 (마이페이지 내 등록 공간 카드) && 카드 상세는 space에 구현
    @GetMapping("/spaces")
    public ResponseEntity<List<Space>> loadMySpaceInProfile(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(service.findMySpaces(email));
    }

    // 예약 통계 추가
}
