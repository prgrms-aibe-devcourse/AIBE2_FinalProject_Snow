package com.snow.popin.domain.popup.dto.response;

import com.snow.popin.domain.popup.entity.PopupStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PopupSummaryResponseDto {
    private Long id;
    private String title;
    private String summary;
    private String period;
    private PopupStatus status;
    private String mainImageUrl;
    private Boolean isFeatured;
    private Boolean reservationAvailable;
    private Boolean waitlistAvailable;

    private Integer entryFee;
    private Boolean isFreeEntry;
    private String feeDisplayText;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PopupImageResponseDto> images;

    private String venueName;
    private String venueAddress;
    private String region;
    private Boolean parkingAvailable;
}
