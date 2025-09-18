package com.snow.popin.domain.popup.service;

import com.snow.popin.domain.popup.dto.request.PopupListRequestDto;
import com.snow.popin.domain.popup.dto.response.*;
import com.snow.popin.domain.popup.entity.Popup;
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

    public PopupListResponseDto getPopupList(PopupListRequestDto request) {
        log.info("팝업 리스트 조회 시작 - 상태: {}, 지역: {}, 날짜필터: {}, 정렬: {}",
                request.getStatus(), request.getRegion(), request.getDateFilter(), request.getSortBy());

        Pageable pageable = createPageableWithSort(request.getPage(), request.getSize(), request.getSortBy());
        Page<Popup> popupPage = findPopupsWithFilters(request, pageable);

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                .stream()
                .map(PopupSummaryResponseDto::from)
                .collect(Collectors.toList());

        log.info("팝업 리스트 조회 완료 - 총 {}개, 페이지 {}개",
                popupPage.getTotalElements(), popupDtos.size());

        return PopupListResponseDto.of(popupPage, popupDtos);
    }

    @Transactional
    public PopupDetailResponseDto getPopupDetail(Long popupId) {
        Popup popup = popupRepository.findByIdWithDetails(popupId)
                .orElseThrow(() -> new PopupNotFoundException(popupId));

        // 실시간으로 상태 업데이트 확인
        boolean statusChanged = popup.updateStatus();
        if (statusChanged) {
            log.info("팝업 ID {}의 상태가 실시간으로 업데이트되었습니다: {}", popup.getId(), popup.getStatus());
            popupRepository.save(popup); // 변경된 경우 저장
        }

        log.info("팝업 상세 조회 완료 - ID: {}, 제목: {}", popup.getId(), popup.getTitle());

        return PopupDetailResponseDto.from(popup);
    }

    // 유사한 팝업 조회
    public PopupListResponseDto getSimilarPopups(String categoryName, Long excludePopupId, int page, int size) {
        log.info("유사한 팝업 조회 - 카테고리: {}, 제외 ID: {}", categoryName, excludePopupId);

        if (categoryName == null || categoryName.trim().isEmpty()) {
            log.warn("카테고리명이 없어서 빈 결과를 반환합니다.");
            return PopupListResponseDto.empty();
        }

        try {
            Pageable pageable = createPageable(page, size);
            Page<Popup> popupPage = popupRepository.findSimilarPopups(categoryName, excludePopupId, pageable);

            List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                    .stream()
                    .map(PopupSummaryResponseDto::from)
                    .collect(Collectors.toList());

            log.info("유사한 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
            return PopupListResponseDto.of(popupPage, popupDtos);

        } catch (Exception e) {
            log.error("유사한 팝업 조회 실패 - 카테고리: {}, 오류: {}", categoryName, e.getMessage());
            return PopupListResponseDto.empty();
        }
    }

    // 카테고리별 팝업 조회
    public PopupListResponseDto getPopupsByCategory(String categoryName, int page, int size) {
        log.info("카테고리별 팝업 조회 - 카테고리: {}", categoryName);

        try {
            Pageable pageable = createPageable(page, size);
            Page<Popup> popupPage = popupRepository.findByCategoryName(categoryName, pageable);

            List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                    .stream()
                    .map(PopupSummaryResponseDto::from)
                    .collect(Collectors.toList());

            log.info("카테고리별 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
            return PopupListResponseDto.of(popupPage, popupDtos);

        } catch (Exception e) {
            log.error("카테고리별 팝업 조회 실패 - 카테고리: {}, 오류: {}", categoryName, e.getMessage());
            return PopupListResponseDto.empty();
        }
    }

    // 지역별 팝업 조회
    public List<PopupSummaryResponseDto> getPopupsByRegion(String region) {
        log.info("지역별 팝업 조회 - 지역: {}", region);

        try {
            List<Popup> popups = popupRepository.findByRegion(region);

            List<PopupSummaryResponseDto> result = popups.stream()
                    .map(PopupSummaryResponseDto::from)
                    .collect(Collectors.toList());

            log.info("지역별 팝업 조회 완료 - 총 {}개", result.size());
            return result;

        } catch (Exception e) {
            log.error("지역별 팝업 조회 실패 - 지역: {}, 오류: {}", region, e.getMessage());
            return Collections.emptyList();
        }
    }

    // 인기 팝업 조회 (isFeatured = true)
    public PopupListResponseDto getPopularPopups(int page, int size) {
        log.info("인기 팝업 조회 시작 - page: {}, size: {}", page, size);

        Pageable pageable = createPageable(page, size);
        Page<Popup> popupPage = popupRepository.findPopularPopups(pageable);

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                .stream()
                .map(PopupSummaryResponseDto::from)
                .collect(Collectors.toList());

        log.info("인기 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
        return PopupListResponseDto.of(popupPage, popupDtos);
    }

    // 사용자 관심 카테고리 기반 추천 팝업 조회
    public PopupListResponseDto getRecommendedPopups(String token, List<Long> overrideCategoryIds, int page, int size) {
        log.info("추천 팝업 조회 시작 - token 존재: {}, 카테고리 override: {}", token != null, overrideCategoryIds);

        Pageable pageable = createPageable(page, size);
        Page<Popup> popupPage;

        if (overrideCategoryIds != null && !overrideCategoryIds.isEmpty()) {
            // 명시적으로 카테고리가 지정된 경우
            popupPage = popupRepository.findRecommendedPopupsByCategories(overrideCategoryIds, pageable);
        } else if (token != null && !token.isEmpty()) {
            // 로그인한 사용자의 관심 카테고리 기반
            List<Long> userInterestCategoryIds = getUserInterestCategoryIds(token);
            if (userInterestCategoryIds.isEmpty()) {
                popupPage = popupRepository.findDefaultRecommendedPopups(pageable);
            } else {
                popupPage = popupRepository.findRecommendedPopupsByCategories(userInterestCategoryIds, pageable);
            }
        } else {
            // 비로그인 사용자 기본 추천
            popupPage = popupRepository.findDefaultRecommendedPopups(pageable);
        }

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                .stream()
                .map(PopupSummaryResponseDto::from)
                .collect(Collectors.toList());

        log.info("추천 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
        return PopupListResponseDto.of(popupPage, popupDtos);
    }

    // 선택된 카테고리 기반 추천 팝업 조회
    public PopupListResponseDto getRecommendedPopupsBySelectedCategories(List<Long> categoryIds, int page, int size) {
        log.info("선택된 카테고리 기반 추천 팝업 조회 - categoryIds: {}", categoryIds);

        Pageable pageable = createPageable(page, size);
        if (categoryIds == null || categoryIds.isEmpty()) {
            Page<Popup> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            return PopupListResponseDto.of(emptyPage, List.of());
        }

        Page<Popup> popupPage = popupRepository.findRecommendedPopupsByCategories(categoryIds, pageable);

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                .stream()
                .map(PopupSummaryResponseDto::from)
                .collect(Collectors.toList());

        log.info("선택된 카테고리 추천 팝업 조회 완료 - 총 {}개", popupPage.getTotalElements());
        return PopupListResponseDto.of(popupPage, popupDtos);
    }


    // TODO: 실제 구현 필요
    private List<Long> getUserInterestCategoryIds(String token) {
        // JWT 토큰에서 사용자 관심 카테고리 조회 로직
        return List.of(1L, 2L, 3L); // 임시값
    }

    private Page<Popup> findPopupsWithFilters(PopupListRequestDto request, Pageable pageable) {
        if (request.isDeadlineSoon()) {
            LocalDate deadline = LocalDate.now().plusDays(7);
            return popupRepository.findDeadlineSoonPopups(deadline, pageable);
        }

        return popupRepository.findWithFilters(
                request.getStatus(),
                request.hasRegionFilter() ? request.getRegion() : null,
                request.getStartDate(),
                request.getEndDate(),
                pageable
        );
    }

    private Pageable createPageable(int page, int size) {
        int validPage = Math.max(0, page);
        int validSize = Math.min(Math.max(1, size), 100);
        return PageRequest.of(validPage, validSize);
    }

    private Pageable createPageableWithSort(int page, int size, String sortBy) {
        int validPage = Math.max(0, page);
        int validSize = Math.min(Math.max(1, size), 100);

        Sort sort = createSort(sortBy);
        return PageRequest.of(validPage, validSize, sort);
    }

    private Sort createSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        switch (sortBy) {
            case "deadline":
                return Sort.by(Sort.Direction.ASC, "endDate");
            case "date":
                return Sort.by(Sort.Direction.ASC, "startDate");
            case "latest":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}