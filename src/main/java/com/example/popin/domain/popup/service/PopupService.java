package com.example.popin.domain.popup.service;

import com.example.popin.domain.popup.dto.request.PopupListRequestDto;
import com.example.popin.domain.popup.dto.response.*;
import com.example.popin.domain.popup.entity.*;
import com.example.popin.domain.popup.repository.PopupRepository;
import com.example.popin.global.exception.PopupNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PopupService {

    private final PopupRepository popupRepository;

    public PopupListResponseDto getPopupList(PopupListRequestDto request) {
        Pageable pageable = createPageable(request);
        Page<Popup> popupPage = getPopups(request.getStatus(), pageable);

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                .stream()
                .map(this::convertToSummaryResponseDto)
                .collect(Collectors.toList());

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

    private Pageable createPageable(PopupListRequestDto request) {
        Sort sort = Sort.by(
                request.getSortDirection().equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                request.getSortBy()
        );
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }
}
