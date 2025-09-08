package com.snow.popin.domain.popup.service;

import com.snow.popin.domain.popup.dto.request.PopupListRequestDto;
import com.snow.popin.domain.popup.dto.request.PopupSearchRequestDto;
import com.snow.popin.domain.popup.dto.response.*;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupHours;
import com.snow.popin.domain.popup.entity.PopupImage;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.global.exception.PopupNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PopupService {

    private final PopupRepository popupRepository;

    // 팝업 리스트 조회 - 모든 필터링을 하나의 메서드에서 처리
    public PopupListResponseDto getPopupList(PopupListRequestDto request) {
        log.info("팝업 리스트 조회 - 상태: {}, 지역: {}, 날짜필터: {}, 정렬: {}",
                request.getStatus(), request.getRegion(), request.getDateFilter(), request.getSortBy());

        Pageable pageable = createPageable(request);
        Page<Popup> popupPage;

        if (request.isDeadlineSoon()) {
            LocalDate deadline = LocalDate.now().plusDays(7);
            popupPage = popupRepository.findDeadlineSoonPopups(deadline, pageable);
        } else {
            popupPage = popupRepository.findWithFilters(
                    request.getStatus(),
                    request.hasRegionFilter() ? request.getRegion() : null,
                    request.getStartDate(),
                    request.getEndDate(),
                    pageable
            );
        }

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent().stream()
                .map(PopupSummaryResponseDto::from) // DTO의 정적 팩토리 메서드 사용
                .collect(Collectors.toList());

        log.info("팝업 리스트 조회 완료 - 총 {}개, 페이지 {}개",
                popupPage.getTotalElements(), popupDtos.size());

        return PopupListResponseDto.of(popupPage, popupDtos); // DTO의 정적 팩토리 메서드 사용
    }

    //팝업 상세 조회
    public PopupDetailResponseDto getPopupDetail(Long popupId) {
        Popup popup = popupRepository.findByIdWithDetails(popupId)
                .orElseThrow(() -> new PopupNotFoundException(popupId));

        log.info("팝업 상세 조회 완료 - ID: {}, 제목: {}", popup.getId(), popup.getTitle());

        return PopupDetailResponseDto.from(popup); // DTO의 정적 팩토리 메서드 사용
    }

    // 추천 팝업 조회
    public PopupListResponseDto getFeaturedPopups(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        Page<Popup> popupPage = popupRepository.findFeaturedPopups(pageable);

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent().stream()
                .map(PopupSummaryResponseDto::from)
                .collect(Collectors.toList());

        return PopupListResponseDto.of(popupPage, popupDtos);
    }

    private Pageable createPageable(PopupListRequestDto request) {
        int page = Math.max(0, request.getPage());
        int size = Math.min(Math.max(1, request.getSize()), 100);

        // 정렬 로직을 서비스 레이어에서 처리
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 기본 정렬
        if ("deadline".equals(request.getSortBy())) {
            sort = Sort.by(Sort.Direction.ASC, "endDate");
        } else if ("date".equals(request.getSortBy())) {
            sort = Sort.by(Sort.Direction.ASC, "startDate");
        }

        return PageRequest.of(page, size, sort);
    }
}