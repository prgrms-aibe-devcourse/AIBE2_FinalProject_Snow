package com.snow.popin.domain.mypage.host.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PopupRegisterRequestDto {
    private Long venueId;
    private String title;
    private String summary;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer entryFee;
    private Boolean reservationAvailable;
    private String reservationLink;
    private Boolean waitlistAvailable;
    private String notice;
    private String mainImageUrl;
    private Boolean isFeatured;

    private List<String> imageUrls;
    private List<PopupHourResponseDto> hours;
    //태그 정해지면 추가
    //private List<String> tags;
}
