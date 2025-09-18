package com.snow.popin.domain.popup.controller;

import com.snow.popin.domain.popup.dto.request.PopupListRequestDto;
import com.snow.popin.domain.popup.dto.request.PopupSearchRequestDto;
import com.snow.popin.domain.popup.dto.response.PopupDetailResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupListResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupSummaryResponseDto;
import com.snow.popin.domain.popup.service.PopupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.Validator;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/popups")
@RequiredArgsConstructor
public class PopupController {

    private final PopupService popupService;

    // 팝업스토어 리스트 조회
    @GetMapping
    public ResponseEntity<PopupListResponseDto> getPopupList(@Valid PopupListRequestDto request) {
        PopupListResponseDto response = popupService.getPopupList(request);
        return ResponseEntity.ok(response);
    }

    // 팝업스토어 상세 조회
    @GetMapping("/{popupId}")
    public ResponseEntity<PopupDetailResponseDto> getPopupDetail(@PathVariable Long popupId) {
        PopupDetailResponseDto response = popupService.getPopupDetail(popupId);
        return ResponseEntity.ok(response);
    }

    // 유사한 팝업 조회
    @GetMapping("/{popupId}/similar")
    public ResponseEntity<PopupListResponseDto> getSimilarPopups(
            @PathVariable Long popupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {

        try {
            PopupDetailResponseDto currentPopup = popupService.getPopupDetail(popupId);

            if (currentPopup.getCategoryName() == null) {
                return ResponseEntity.ok(PopupListResponseDto.empty(page, size));
            }

            PopupListResponseDto response = popupService.getSimilarPopups(
                    currentPopup.getCategoryName(), popupId, page, size);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.ok(PopupListResponseDto.empty(page, size));
        }
    }

    // 카테고리별 팝업 조회
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<PopupListResponseDto> getPopupsByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("카테고리별 팝업 조회 API 호출 - 카테고리: {}, 페이지: {}, 크기: {}", categoryName, page, size);
        PopupListResponseDto response = popupService.getPopupsByCategory(categoryName, page, size);
        return ResponseEntity.ok(response);
    }

    // 지역별 팝업 조회
    @GetMapping("/region/{region}")
    public ResponseEntity<List<PopupSummaryResponseDto>> getPopupsByRegion(@PathVariable String region) {
        log.info("지역별 팝업 조회 API 호출 - 지역: {}", region);
        List<PopupSummaryResponseDto> popups = popupService.getPopupsByRegion(region);
        return ResponseEntity.ok(popups);
    }

    // 인기 팝업 조회 (isFeatured = true)
    @GetMapping("/popular")
    public ResponseEntity<PopupListResponseDto> getPopularPopups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PopupListResponseDto response = popupService.getPopularPopups(page, size);
        return ResponseEntity.ok(response);
    }

    // 추천 팝업 조회 (사용자 관심 카테고리 기반)
    @GetMapping("/recommended")
    public ResponseEntity<PopupListResponseDto> getRecommendedPopups(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String cleanToken = (token != null && token.startsWith("Bearer ")) ? token.substring(7) : token;
        PopupListResponseDto response = popupService.getRecommendedPopups(cleanToken, categoryIds, page, size);
        return ResponseEntity.ok(response);
    }

    // 추천 팝업 조회 (선택된 카테고리 기반)
    @GetMapping("/recommended/by-categories")
    public ResponseEntity<PopupListResponseDto> getRecommendedPopupsByCategories(
            @RequestParam List<Long> categoryIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PopupListResponseDto response = (categoryIds == null || categoryIds.isEmpty())
                ? popupService.getRecommendedPopups(null, null, page, size)
                : popupService.getRecommendedPopupsBySelectedCategories(categoryIds, page, size);
        return ResponseEntity.ok(response);
    }
}
