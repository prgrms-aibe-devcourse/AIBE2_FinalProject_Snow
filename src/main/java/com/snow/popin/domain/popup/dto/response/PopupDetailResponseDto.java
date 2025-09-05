package com.snow.popin.domain.popup.dto.response;

import com.snow.popin.domain.popup.entity.PopupStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PopupDetailResponseDto {
    private Long id;
    private Long brandId;
    private Long venueId;
    private String title;
    private String summary;
    private String description;
    private String period;
    private PopupStatus status;
    private String mainImageUrl;
    private Boolean isFeatured;

    private Boolean reservationAvailable;
    private String reservationLink;
    private Boolean waitlistAvailable;

    private Integer entryFee;
    private Boolean isFreeEntry;
    private String feeDisplayText;

    private String notice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PopupImageResponseDto> images;
    private List<PopupHoursResponseDto> hours;
}