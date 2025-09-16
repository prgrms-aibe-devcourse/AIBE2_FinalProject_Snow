package com.snow.popin.domain.mypage.host.dto;

import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.Tag;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    private List<String> imageUrls;
    private List<PopupHourResponseDto> hours;
    private List<String> tags;

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
                // 추가된 필드들
                .imageUrls(
                        popup.getImages().stream()
                                .map(img -> img.getImageUrl())
                                .collect(Collectors.toList())
                )
                .hours(
                        popup.getHours().stream()
                                .map(PopupHourResponseDto::fromEntity)
                                .collect(Collectors.toList())
                )
                //태그 추후 추가
//                .tags(
//                        popup.getTags().stream()
//                                .map(Tag::getName)
//                                .collect(Collectors.toList())
//                )
                .build();
    }
}
