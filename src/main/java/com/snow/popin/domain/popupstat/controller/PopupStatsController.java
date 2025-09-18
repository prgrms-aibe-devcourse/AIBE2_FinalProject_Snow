package com.snow.popin.domain.popupstat.controller;

import com.snow.popin.domain.popupstat.dto.PopupStatsResponseDto;
import com.snow.popin.domain.popupstat.service.PopupStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/host/popups")
@RequiredArgsConstructor
public class PopupStatsController {

    private final PopupStatsService popupStatsService;

    /**
     * 특정 팝업의 통계를 조회한다.
     *
     * @param popupId 팝업 ID
     * @param start   조회 시작일
     * @param end     조회 종료일
     * @return PopupStatsResponseDto
     */
    @GetMapping("/{popupId}/stats")
    public ResponseEntity<List<PopupStatsResponseDto>> getPopupStats(
            @PathVariable Long popupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        List<PopupStatsResponseDto> stats = popupStatsService.getStats(popupId, start, end);
        return ResponseEntity.ok(stats);
    }
}
