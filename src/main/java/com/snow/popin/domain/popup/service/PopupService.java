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

    public PopupDetailResponseDto getPopupDetail(Long popupId) {
        Popup popup = popupRepository.findByIdWithDetails(popupId)
                .orElseThrow(() -> new PopupNotFoundException(popupId));

        log.info("팝업 상세 조회 완료 - ID: {}, 제목: {}", popup.getId(), popup.getTitle());

        return PopupDetailResponseDto.from(popup);
    }

    public PopupListResponseDto getFeaturedPopups(int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<Popup> popupPage = popupRepository.findFeaturedPopups(pageable);

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                .stream()
                .map(PopupSummaryResponseDto::from)
                .collect(Collectors.toList());

        return PopupListResponseDto.of(popupPage, popupDtos);
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