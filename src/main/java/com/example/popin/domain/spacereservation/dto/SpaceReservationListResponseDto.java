package com.example.popin.domain.spacereservation.dto;

import com.example.popin.domain.spacereservation.entity.SpaceReservation;
import com.example.popin.domain.spacereservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SpaceReservationListResponseDto {
    private Long id;
    private String brand;
    private String popupTitle;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private ReservationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 간단한 공간 정보
    private String spaceTitle;
    private String spaceAddress;
    private String spaceImageUrl;

    // 예약자 정보 (PROVIDER가 볼 때)
    private String hostName;
    private String hostPhone;

    //예약 리스트를 HOST와 PROVIDER 둘 다 확인함
    // Entity -> DTO 변환 (HOST 용 - 내가 신청한 예약)
    public static SpaceReservationListResponseDto fromForHost(SpaceReservation reservation) {
        return SpaceReservationListResponseDto.builder()
                .id(reservation.getId())
                .brand(reservation.getBrand())
                .popupTitle(reservation.getPopupTitle())
                .startDate(reservation.getStartDate())
                .endDate(reservation.getEndDate())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .spaceTitle(reservation.getSpace().getTitle())
                .spaceAddress(reservation.getSpace().getAddress())
                .spaceImageUrl(reservation.getSpace().getCoverImageUrl())
                .build();
    }

    // Entity -> DTO 변환 (PROVIDER 용 - 내 공간에 신청된 예약)
    public static SpaceReservationListResponseDto fromForProvider(SpaceReservation reservation) {
        return SpaceReservationListResponseDto.builder()
                .id(reservation.getId())
                .brand(reservation.getBrand())
                .popupTitle(reservation.getPopupTitle())
                .startDate(reservation.getStartDate())
                .endDate(reservation.getEndDate())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .spaceTitle(reservation.getSpace().getTitle())
                .spaceAddress(reservation.getSpace().getAddress())
                .spaceImageUrl(reservation.getSpace().getCoverImageUrl())
                .hostName(reservation.getHost().getName())
                .hostPhone(reservation.getContactPhone() != null ?
                        reservation.getContactPhone() : reservation.getHost().getPhone())
                .build();
    }
}