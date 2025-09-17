package com.snow.popin.domain.popupReservation.entity;

import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.common.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservation_user_id", columnList = "user_id"),
        @Index(name = "idx_reservation_popup_id", columnList = "popup_id"),
        @Index(name = "idx_reservation_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_id", nullable = false)
    private Popup popup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    // 예약 희망일자
    @Column(name = "reservation_date")
    private LocalDateTime reservationDate;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    public boolean canCancel() {
        return status == ReservationStatus.RESERVED;
    }

    public void cancel() {
        if (!canCancel()) {
            throw new IllegalStateException("취소할 수 없는 예약입니다.");
        }
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == ReservationStatus.RESERVED;
    }

    public void markAsVisited() {
        this.status = ReservationStatus.VISITED;
    }

    public static Reservation create(Popup popup, User user, String name, String phone, Integer partySize, LocalDateTime reservationDate) {
        return Reservation.builder()
                .popup(popup)
                .user(user)
                .name(name)
                .phone(phone)
                .partySize(partySize)
                .reservationDate(reservationDate)
                .status(ReservationStatus.RESERVED)
                .reservedAt(LocalDateTime.now())
                .build();
    }
}
