package com.snow.popin.domain.popup.service;

import com.snow.popin.domain.popup.dto.request.PopupListRequestDto;
import com.snow.popin.domain.popup.dto.request.PopupSearchRequestDto;
import com.snow.popin.domain.popup.dto.response.*;
import com.snow.popin.domain.popup.entity.*;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PopupService {

    private final PopupRepository popupRepository;

    public PopupListResponseDto getPopupList(PopupListRequestDto request) {
        Pageable pageable = createPageable(request.getPage(), request.getSize(),
                request.getSortBy(), request.getSortDirection());
        Page<Popup> popupPage = getPopups(request.getStatus(), pageable);

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                .stream()
                .map(this::convertToSummaryResponseDto)
                .collect(Collectors.toList());

        return buildPopupListResponse(popupPage, popupDtos);
    }

    public PopupListResponseDto searchPopups(PopupSearchRequestDto request) {
        Pageable pageable = createPageable(request.getPage(), request.getSize(),
                request.getSortBy(), request.getSortDirection());

        // 입력값 정제 및 정규화
        String title = (request.getTitle() != null && !request.getTitle().trim().isEmpty())
                ? request.getTitle().trim() : null;
        String region = (request.getRegion() != null && !request.getRegion().trim().isEmpty())
                ? request.getRegion().trim() : null;
        List<String> tags = CollectionUtils.isEmpty(request.getTags()) ? null
                : request.getTags().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Page<Popup> popupPage;

        // 태그가 있는 경우와 없는 경우로 분리
        if (!CollectionUtils.isEmpty(tags)) {
            popupPage = popupRepository.searchPopupsByTags(tags, title, region, pageable);
        } else {
            popupPage = popupRepository.searchPopups(title, region, pageable);
        }

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                .stream()
                .map(this::convertToSummaryResponseDto)
                .collect(Collectors.toList());

        log.info("팝업 검색 완료 - 제목: {}, 지역: {}, 태그: {}, 결과 수: {}",
                title, region, tags, popupPage.getTotalElements());

        return buildPopupListResponse(popupPage, popupDtos);
    }

    public PopupDetailResponseDto getPopupDetail(Long popupId) {
        Popup popup = popupRepository.findByIdWithDetails(popupId)
                .orElseThrow(() -> new PopupNotFoundException(popupId));

        log.info("팝업 상세 조회 완료 - ID: {}, 제목: {}, 이미지 수: {}, 운영시간 수: {}",
                popup.getId(), popup.getTitle(), popup.getImages().size(), popup.getHours().size());
        return convertToDetailResponseDto(popup);
    }

    private Page<Popup> getPopups(PopupStatus status, Pageable pageable) {
        if (status != null) {
            return popupRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            return popupRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
    }

    private PopupListResponseDto buildPopupListResponse(Page<Popup> popupPage, List<PopupSummaryResponseDto> popupDtos) {
        return PopupListResponseDto.builder()
                .popups(popupDtos)
                .totalPages(popupPage.getTotalPages())
                .totalElements(popupPage.getTotalElements())
                .currentPage(popupPage.getNumber())
                .size(popupPage.getSize())
                .hasNext(popupPage.hasNext())
                .hasPrevious(popupPage.hasPrevious())
                .build();
    }

    private PopupSummaryResponseDto convertToSummaryResponseDto(Popup popup) {
        List<PopupImageResponseDto> imageDtos = popup.getImages().stream()
                .map(this::convertToImageResponseDto)
                .collect(Collectors.toList());

        return PopupSummaryResponseDto.builder()
                .id(popup.getId())
                .title(popup.getTitle())
                .summary(popup.getSummary())
                .period(popup.getPeriod())
                .status(popup.getStatus())
                .mainImageUrl(popup.getMainImageUrl())
                .isFeatured(popup.getIsFeatured())
                .reservationAvailable(popup.getReservationAvailable())
                .waitlistAvailable(popup.getWaitlistAvailable())
                .entryFee(popup.getEntryFee())
                .isFreeEntry(popup.isFreeEntry())
                .feeDisplayText(popup.getFeeDisplayText())
                .createdAt(popup.getCreatedAt())
                .updatedAt(popup.getUpdatedAt())
                .images(imageDtos)
                .build();
    }

    private PopupDetailResponseDto convertToDetailResponseDto(Popup popup) {
        List<PopupImageResponseDto> imageDtos = popup.getImages().stream()
                .map(this::convertToImageResponseDto)
                .collect(Collectors.toList());

        List<PopupHoursResponseDto> hoursDtos = popup.getHours().stream()
                .map(this::convertToHoursResponseDto)
                .collect(Collectors.toList());

        return PopupDetailResponseDto.builder()
                .id(popup.getId())
                .brandId(popup.getBrandId())
                .venueId(popup.getVenueId())
                .title(popup.getTitle())
                .summary(popup.getSummary())
                .description(popup.getDescription())
                .period(popup.getPeriod())
                .status(popup.getStatus())
                .mainImageUrl(popup.getMainImageUrl())
                .isFeatured(popup.getIsFeatured())
                .reservationAvailable(popup.getReservationAvailable())
                .reservationLink(popup.getReservationLink())
                .waitlistAvailable(popup.getWaitlistAvailable())
                .entryFee(popup.getEntryFee())
                .isFreeEntry(popup.isFreeEntry())
                .feeDisplayText(popup.getFeeDisplayText())
                .notice(popup.getNotice())
                .createdAt(popup.getCreatedAt())
                .updatedAt(popup.getUpdatedAt())
                .images(imageDtos)
                .hours(hoursDtos)
                .build();
    }

    private PopupImageResponseDto convertToImageResponseDto(PopupImage image) {
        return PopupImageResponseDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .caption(image.getCaption())
                .sortOrder(image.getSortOrder())
                .build();
    }

    private PopupHoursResponseDto convertToHoursResponseDto(PopupHours hours) {
        return PopupHoursResponseDto.builder()
                .id(hours.getId())
                .dayOfWeek(hours.getDayOfWeek())
                .openTime(hours.getOpenTime())
                .closeTime(hours.getCloseTime())
                .note(hours.getNote())
                .build();
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        String prop = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        Sort.Direction dir = ("ASC".equalsIgnoreCase(sortDirection)) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), 100);
        return PageRequest.of(p, s, Sort.by(dir, prop));
    }
}