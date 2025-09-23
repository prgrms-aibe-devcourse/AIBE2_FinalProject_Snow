package com.snow.popin.domain.popup.service;

import com.snow.popin.domain.popup.dto.response.*;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.global.exception.PopupNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PopupService {

    private final PopupRepository popupRepository;

    // ===== 메인 페이지 필터링 API =====

    // 전체 팝업 조회
    public PopupListResponseDto getAllPopups(int page, int size, PopupStatus status) {
        log.info("전체 팝업 조회 - page: {}, size: {}, status: {}", page, size, status);

        Pageable pageable = createPageable(page, size);
        Page<Popup> popupPage = popupRepository.findAllWithStatusFilter(status, pageable);

        List<PopupSummaryResponseDto> popupDtos = convertToSummaryDtos(popupPage.getContent());

        log.info("전체 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
        return PopupListResponseDto.of(popupPage, popupDtos);
    }

    // 인기 팝업 조회
    public PopupListResponseDto getPopularPopups(int page, int size) {
        log.info("인기 팝업 조회 시작 - page: {}, size: {}", page, size);

        // 인기 팝업은 최대 20개로 제한
        int maxPopularItems = 20;
        int remainingItems = maxPopularItems - (page * size);

        if (remainingItems <= 0) {
            return PopupListResponseDto.empty(page, size);
        }

        int adjustedSize = Math.min(size, remainingItems);
        Pageable pageable = PageRequest.of(page, adjustedSize);

        // 진행중/예정 상태만 조회하는 새 메서드 사용
        Page<Popup> popupPage = popupRepository.findPopularActivePopups(pageable);

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                .stream()
                .map(PopupSummaryResponseDto::from)
                .collect(Collectors.toList());

        log.info("인기 팝업 조회 완료 - 총 {}개 (ONGOING/PLANNED만)", popupDtos.size());

        return PopupListResponseDto.of(popupPage, popupDtos);
    }

    // 마감임박 팝업 조회
    public PopupListResponseDto getDeadlineSoonPopups(int page, int size, PopupStatus status) {
        log.info("마감임박 팝업 조회 - page: {}, size: {}, status: {}", page, size, status);

        Pageable pageable = createPageable(page, size);
        Page<Popup> popupPage = popupRepository.findDeadlineSoonPopups(status, pageable);

        List<PopupSummaryResponseDto> popupDtos = convertToSummaryDtos(popupPage.getContent());

        log.info("마감임박 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
        return PopupListResponseDto.of(popupPage, popupDtos);
    }

    // 지역별 + 날짜별 팝업 조회
    public PopupListResponseDto getPopupsByRegionAndDate(
            String region, PopupStatus status, String dateFilter,
            LocalDate customStartDate, LocalDate customEndDate,
            int page, int size) {

        log.info("지역별 날짜별 팝업 조회 - region: {}, dateFilter: {}", region, dateFilter);

        // 날짜 범위 계산
        LocalDate[] dateRange = calculateDateRange(dateFilter, customStartDate, customEndDate);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        Pageable pageable = createPageable(page, size);

        // status 파라미터 제거하고 호출
        Page<Popup> popupPage = popupRepository.findByRegionAndDateRange(
                region, startDate, endDate, pageable);

        List<PopupSummaryResponseDto> popupDtos = convertToSummaryDtos(popupPage.getContent());

        log.info("지역별 날짜별 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
        return PopupListResponseDto.of(popupPage, popupDtos);
    }

    /**
     * AI 추천 팝업 조회 (현재는 인기 팝업으로 대체)
     * TODO: 향후 실제 AI 추천 로직으로 교체 예정
     */
    public PopupListResponseDto getAIRecommendedPopups(String token, int page, int size) {
        log.info("AI 추천 팝업 조회 - page: {}, size: {} (현재는 인기 팝업으로 대체)", page, size);

        // TODO: JWT 토큰에서 사용자 관심사 추출하여 AI 추천 로직 구현
        // 현재는 진행중인 인기 팝업을 반환
        return getPopularPopups(page, size);
    }

    // ===== 팝업 상세 조회 =====

    // 팝업 상세 조회 (조회수 증가)
    @Transactional
    public PopupDetailResponseDto getPopupDetail(Long popupId) {
        log.info("팝업 상세 조회 - popupId: {}", popupId);

        Popup popup = popupRepository.findByIdWithDetails(popupId)
                .orElseThrow(() -> new PopupNotFoundException(popupId));

        // 실시간 상태 업데이트 확인
        boolean statusChanged = popup.updateStatus();
        if (statusChanged) {
            log.info("팝업 ID {}의 상태가 실시간으로 업데이트됨: {}", popup.getId(), popup.getStatus());
            popupRepository.save(popup);
        }

        // 조회수 증가
        popup.incrementViewCount();
        log.info("팝업 조회수 증가 - ID: {}, 현재 조회수: {}", popup.getId(), popup.getViewCount());

        return PopupDetailResponseDto.from(popup);
    }

    // 팝업 상세 조회 (조회수 증가 없음 - 관리자용)
    public PopupDetailResponseDto getPopupDetailForAdmin(Long popupId) {
        log.info("팝업 상세 조회 (관리자) - popupId: {}", popupId);

        Popup popup = popupRepository.findByIdWithDetails(popupId)
                .orElseThrow(() -> new PopupNotFoundException(popupId));

        return PopupDetailResponseDto.from(popup);
    }

    // ===== 추천 및 유사 팝업 조회 =====

    // 유사한 팝업 조회 (같은 카테고리)
    public PopupListResponseDto getSimilarPopups(String categoryName, Long excludePopupId, int page, int size) {
        log.info("유사한 팝업 조회 - 카테고리: {}, 제외 ID: {}", categoryName, excludePopupId);

        if (categoryName == null || categoryName.trim().isEmpty()) {
            log.warn("카테고리명이 없어서 빈 결과 반환");
            return PopupListResponseDto.empty(0, 20);
        }

        try {
            Pageable pageable = createPageable(page, size);
            Page<Popup> popupPage = popupRepository.findSimilarPopups(categoryName, excludePopupId, pageable);

            List<PopupSummaryResponseDto> popupDtos = convertToSummaryDtos(popupPage.getContent());

            log.info("유사한 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
            return PopupListResponseDto.of(popupPage, popupDtos);

        } catch (Exception e) {
            log.error("유사한 팝업 조회 실패 - 카테고리: {}", categoryName, e);
            return PopupListResponseDto.empty(page, size);
        }
    }

    // 카테고리별 추천 팝업 조회
    public PopupListResponseDto getRecommendedPopupsBySelectedCategories(
            List<Long> categoryIds, int page, int size) {
        log.info("선택된 카테고리 기반 추천 팝업 조회 - categoryIds: {}", categoryIds);

        Pageable pageable = createPageable(page, size);
        if (categoryIds == null || categoryIds.isEmpty()) {
            Page<Popup> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            return PopupListResponseDto.of(emptyPage, List.of());
        }

        Page<Popup> popupPage = popupRepository.findRecommendedPopupsByCategories(categoryIds, pageable);

        List<PopupSummaryResponseDto> popupDtos = convertToSummaryDtos(popupPage.getContent());

        log.info("선택된 카테고리 추천 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
        return PopupListResponseDto.of(popupPage, popupDtos);
    }

    // ===== 카테고리 및 지역별 조회 =====

    // 카테고리별 팝업 조회
    public PopupListResponseDto getPopupsByCategory(String categoryName, int page, int size) {
        log.info("카테고리별 팝업 조회 - 카테고리: {}", categoryName);

        try {
            Pageable pageable = createPageable(page, size);
            Page<Popup> popupPage = popupRepository.findByCategoryName(categoryName, pageable);

            List<PopupSummaryResponseDto> popupDtos = convertToSummaryDtos(popupPage.getContent());

            log.info("카테고리별 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
            return PopupListResponseDto.of(popupPage, popupDtos);

        } catch (Exception e) {
            log.error("카테고리별 팝업 조회 실패 - 카테고리: {}", categoryName, e);
            return PopupListResponseDto.empty(page, size);
        }
    }

    // 지역별 팝업 조회
    public List<PopupSummaryResponseDto> getPopupsByRegion(String region) {
        log.info("지역별 팝업 조회 - region: {}", region);

        List<Popup> popups = popupRepository.findByRegion(region);

        return popups.stream()
                .map(PopupSummaryResponseDto::from)
                .collect(Collectors.toList());
    }

    // ===== 유틸리티 메서드들 =====

    // PopupStatus 문자열을 Enum으로 변환
    public PopupStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty() || "전체".equals(status)) {
            return null; // 전체 조회
        }

        try {
            return PopupStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 상태값: {}", status);
            return null;
        }
    }

    // 페이지네이션 객체 생성
    private Pageable createPageable(int page, int size) {
        int validPage = Math.max(0, page);
        int validSize = Math.min(Math.max(1, size), 100);
        return PageRequest.of(validPage, validSize);
    }

    // 날짜 범위 계산
    private LocalDate[] calculateDateRange(String dateFilter, LocalDate customStartDate, LocalDate customEndDate) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        // 직접입력인 경우 우선 처리
        if (customStartDate != null && customEndDate != null) {
            startDate = customStartDate;
            endDate = customEndDate;
            return new LocalDate[]{startDate, endDate};
        }

        if (dateFilter != null) {
            LocalDate today = LocalDate.now();
            switch (dateFilter) {
                case "today":
                    startDate = today;
                    endDate = today;
                    break;
                case "7days":
                    startDate = today;
                    endDate = today.plusDays(7);
                    break;
                case "14days":
                    startDate = today;
                    endDate = today.plusDays(14);
                    break;
                case "custom":
                    startDate = customStartDate;
                    endDate = customEndDate;
                    break;
                default:
                    break;
            }
        }

        return new LocalDate[]{startDate, endDate};
    }

    // Popup 리스트를 PopupSummaryResponseDto 리스트로 변환
    private List<PopupSummaryResponseDto> convertToSummaryDtos(List<Popup> popups) {
        return popups.stream()
                .map(PopupSummaryResponseDto::from)
                .collect(Collectors.toList());
    }
}