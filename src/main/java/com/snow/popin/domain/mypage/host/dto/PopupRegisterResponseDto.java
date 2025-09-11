package com.snow.popin.domain.mypage.host.dto;

import com.snow.popin.domain.popup.entity.Popup;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter @Builder
public class PopupRegisterResponseDto {
    private Long id;
    private String title;
    private String summary;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer entryFee;
    private String venueName;
    private String venueAddress;
    private String region;
    private String mainImageUrl;
    private String status;

    public static PopupRegisterResponseDto fromEntity(Popup popup) {
        return PopupRegisterResponseDto.builder()
                .id(popup.getId())
                .title(popup.getTitle())
                .summary(popup.getSummary())
                .description(popup.getDescription())
                .startDate(popup.getStartDate())
                .endDate(popup.getEndDate())
                .entryFee(popup.getEntryFee())
                .venueName(popup.getVenueName())
                .venueAddress(popup.getVenueAddress())
                .region(popup.getRegion())
                .mainImageUrl(popup.getMainImageUrl())
                .status(popup.getStatus().name())
                .build();
    }
}
