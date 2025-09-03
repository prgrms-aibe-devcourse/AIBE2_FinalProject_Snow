package com.example.popin.domain.popup.dto.response;

import com.example.popin.domain.popup.entity.PopupStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
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
}
