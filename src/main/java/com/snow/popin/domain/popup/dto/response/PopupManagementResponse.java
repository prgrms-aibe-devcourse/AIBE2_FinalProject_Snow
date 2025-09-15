package com.snow.popin.domain.popup.dto.response;

import com.snow.popin.domain.category.entity.Category;
import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupImage;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자용 팝업 응답 DTO
 */
@Getter
@Builder
public class PopupManagementResponse {
    private final Long id;
    private final String title;
    private final String summary;
    private final String description;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final PopupStatus status;
    private final String rejectReason;
    private final String mainImageUrl;
    private final Boolean isFeatured;
    private final Boolean reservationAvailable;
    private final Boolean waitlistAvailable;

    private final Integer entryFee;
    private final Boolean isFreeEntry;
    private final String feeDisplayText;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private final String venueName;
    private final String venueAddress;
    private final String region;

    private final Long categoryId;
    private final String categoryName;

    private List<PopupImageResponseDto> images;
    private List<PopupHoursResponseDto> hours;

    // 브랜드 정보 추가
    private final Long brandId;
    private final String brandName;
    private final String brandDescription;
    private final String brandOfficialSite;
    private final String brandLogoUrl;
    private final String brandBusinessType;

    // 주최자 정보
    private final Long hostId;
    private final String hostName;
    private final String hostEmail;


    /**
     * 기본 PopupManagementResponse 생성
     */
    public static PopupManagementResponse from(Popup popup) {
        return PopupManagementResponse.builder()
                .id(popup.getId())
                .title(popup.getTitle())
                .summary(popup.getSummary())
                .description(popup.getDescription())
                .startDate(popup.getStartDate())
                .endDate(popup.getEndDate())
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
                .venueName(popup.getVenueName())
                .venueAddress(popup.getVenueAddress())
                .region(popup.getRegion())
                .categoryId(popup.getCategory() != null ? popup.getCategory().getId() : null)
                .categoryName(popup.getCategoryName())
                .images(popup.getImages() != null ? popup.getImages().stream()
                        .map(PopupImageResponseDto::from)
                        .collect(Collectors.toList()) : null)
                .hours(popup.getHours() != null ? popup.getHours().stream()
                        .map(PopupHoursResponseDto::from)
                        .collect(Collectors.toList()) : null)
                .brandId(popup.getBrandId())
                .build();
    }

    /**
     * 브랜드 정보와 함께 PopupManagementResponse 생성
     */
    public static PopupManagementResponse fromWithBrand(Popup popup, Brand brand) {
        return PopupManagementResponse.builder()
                .id(popup.getId())
                .title(popup.getTitle())
                .summary(popup.getSummary())
                .description(popup.getDescription())
                .startDate(popup.getStartDate())
                .endDate(popup.getEndDate())
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
                .venueName(popup.getVenueName())
                .venueAddress(popup.getVenueAddress())
                .region(popup.getRegion())
                .categoryId(popup.getCategory() != null ? popup.getCategory().getId() : null)
                .categoryName(popup.getCategoryName())
                .images(popup.getImages().stream()
                        .map(PopupImageResponseDto::from)
                        .collect(Collectors.toList()))
                .hours(popup.getHours().stream()
                        .map(PopupHoursResponseDto::from)
                        .collect(Collectors.toList()))
                .brandId(popup.getBrandId())
                .brandName(brand != null ? brand.getName() : null)
                .brandDescription(brand != null ? brand.getDescription() : null)
                .brandOfficialSite(brand != null ? brand.getOfficialSite() : null)
                .brandLogoUrl(brand != null ? brand.getLogoUrl() : null)
                .brandBusinessType(brand != null ? brand.getBusinessType().name() : null)
                .build();
    }
}
