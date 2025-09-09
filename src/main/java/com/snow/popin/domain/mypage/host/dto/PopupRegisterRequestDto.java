package com.snow.popin.domain.mypage.host.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
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
}
