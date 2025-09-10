package com.snow.popin.domain.popupReservation.repository;

import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popupReservation.entity.Reservation;
import com.snow.popin.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 사용자의 예약 목록 조회 (최신순)
    List<Reservation> findByUserOrderByCreatedAtDesc(User user);

    // 특정 팝업의 예약 목록 조회
    List<Reservation> findByPopupOrderByCreatedAtDesc(Popup popup);

    // 사용자가 특정 팝업에 활성 예약이 있는지 확인
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reservation r WHERE r.user = :user AND r.popup = :popup AND r.status = 'BOOKED'")
    boolean existsByUserAndPopupAndStatusBooked(@Param("user") User user, @Param("popup") Popup popup);

    // 팝업별 예약 수 조회
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.popup = :popup AND r.status = 'BOOKED'")
    Long countActiveReservationsByPopup(@Param("popup") Popup popup);

    List<Reservation> findByUser(User currentUser);

    List<Reservation> findByPopup(Popup popup);

    boolean existsByPopupAndUser(Popup popup, User currentUser);
}