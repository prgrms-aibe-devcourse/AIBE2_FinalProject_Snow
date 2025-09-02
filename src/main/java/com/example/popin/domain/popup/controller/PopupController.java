package com.example.popin.domain.popup.controller;

import com.example.popin.domain.popup.dto.request.PopupListRequestDto;
import com.example.popin.domain.popup.dto.response.PopupDetailResponseDto;
import com.example.popin.domain.popup.dto.response.PopupListResponseDto;
import com.example.popin.domain.popup.entity.PopupStatus;
import com.example.popin.domain.popup.service.PopupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/popups")
@RequiredArgsConstructor
public class PopupController {

    private final PopupService popupService;

    // 팝업스토어 리스트 조회
    @GetMapping
    public ResponseEntity<PopupListResponseDto> getPopupList(
            @RequestParam(required = false) PopupStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        PopupListRequestDto request = new PopupListRequestDto();
        request.setStatus(status);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        PopupListResponseDto response = popupService.getPopupList(request);
        return ResponseEntity.ok(response);
    }

    // 팜업스토어 상세 조회
    @GetMapping("/{popupId}")
    public ResponseEntity<PopupDetailResponseDto> getPopupDetail(@PathVariable Long popupId) {
        PopupDetailResponseDto response = popupService.getPopupDetail(popupId);
        return ResponseEntity.ok(response);
    }
}
