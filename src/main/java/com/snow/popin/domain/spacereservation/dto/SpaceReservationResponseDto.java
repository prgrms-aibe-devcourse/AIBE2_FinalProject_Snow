package com.snow.popin.domain.spacereservation.dto;

import com.snow.popin.domain.spacereservation.entity.SpaceReservation;
import com.snow.popin.domain.spacereservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SpaceReservationResponseDto {
    private Long id;
    private String brand;
    private String popupTitle;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String message;
    private String contactPhone;
    private String popupDescription;
    private ReservationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 공간 정보
    private SpaceInfo space;

    // 예약자(HOST) 정보
    private HostInfo host;

    @Getter
    @Builder
    public static class SpaceInfo {
        private Long id;
        private String title;
        private String address;
        private Integer rentalFee;
        private String coverImageUrl;
    }

    @Getter
    @Builder
    public static class HostInfo {
        private Long id;
        private String name;
        private String email;
        private String phone;
    }

    // Entity -> DTO 변환
    public static SpaceReservationResponseDto from(SpaceReservation reservation) {
        return SpaceReservationResponseDto.builder()
                .id(reservation.getId())
                .brand(reservation.getBrand())
                .popupTitle(reservation.getPopupTitle())
                .startDate(reservation.getStartDate())
                .endDate(reservation.getEndDate())
                .message(reservation.getMessage())
                .contactPhone(reservation.getContactPhone())
                .popupDescription(reservation.getPopupDescription())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .space(SpaceInfo.builder()
                        .id(reservation.getSpace().getId())
                        .title(reservation.getSpace().getTitle())
                        .address(reservation.getSpace().getAddress())
                        .rentalFee(reservation.getSpace().getRentalFee())
                        .coverImageUrl(reservation.getSpace().getCoverImageUrl())
                        .build())
                .host(HostInfo.builder()
                        .id(reservation.getHost().getId())
                        .name(reservation.getHost().getName())
                        .email(reservation.getHost().getEmail())
                        .phone(reservation.getHost().getPhone())
                        .build())
                .build();
    }
}