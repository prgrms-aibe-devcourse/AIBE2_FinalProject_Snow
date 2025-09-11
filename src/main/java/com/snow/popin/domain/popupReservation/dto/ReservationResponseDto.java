package com.snow.popin.domain.popupReservation.dto;

import com.snow.popin.domain.popupReservation.entity.Reservation;
import com.snow.popin.domain.popupReservation.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 예약 응답 DTO
 *
 * 예약 정보와 함께 팝업 정보, 상태를 포함
 */
@Getter
@Builder
public class ReservationResponseDto {
    private Long id;
    private Long popupId;
    private String popupTitle;
    private String popupSummary;
    private String venueName;
    private String venueAddress;
    private String name;
    private String phone;
    private LocalDateTime reservationDate;
    private LocalDateTime reservedAt;
    private ReservationStatus status;
    private String statusDescription;

    public static ReservationResponseDto from(Reservation reservation) {
        return ReservationResponseDto.builder()
                .id(reservation.getId())
                .popupId(reservation.getPopup().getId())
                .popupTitle(reservation.getPopup().getTitle())
                .popupSummary(reservation.getPopup().getSummary())
                .venueName(reservation.getPopup().getVenueName())
                .venueAddress(reservation.getPopup().getVenueAddress())
                .name(reservation.getName())
                .phone(reservation.getPhone())
                .reservationDate(reservation.getReservationDate())
                .reservedAt(reservation.getReservedAt())
                .status(reservation.getStatus())
                .statusDescription(reservation.getStatus().getDescription())
                .build();
    }
}