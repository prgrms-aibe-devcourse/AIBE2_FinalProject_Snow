package com.snow.popin.domain.popupReservation.repository;

import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popupReservation.entity.Reservation;
import com.snow.popin.domain.popupReservation.entity.ReservationStatus;
import com.snow.popin.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 레포지토리
 *
 * 예약 엔티티에 대한 CRUD 및 커스텀 조회 메서드 제공
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    /**
     * 특정 사용자의 예약 목록 조회
     *
     * @param currentUser 사용자
     * @return 예약 목록
     */
    List<Reservation> findByUser(User currentUser);
    /**
     * 특정 팝업의 예약 목록 조회
     *
     * @param popup 팝업
     * @return 예약 목록
     */
    List<Reservation> findByPopup(Popup popup);

    /**
     * 특정 시간 범위 내의 예약 조회 (예: 예약 30분 전 확인용)
     */
    @Query("SELECT r FROM Reservation r WHERE FUNCTION('DATE_FORMAT', r.reservationDate, '%Y-%m-%d %H:%i') = :target")
    List<Reservation> findByReservationMinute(@Param("target") String targetMinute);

    /**
     * 특정 팝업과 사용자에 대해 예약 존재 여부 확인
     *
     * @param popup 팝업
     * @param currentUser 사용자
     * @return 존재 여부
     */
    boolean existsByPopupAndUser(Popup popup, User currentUser);

    //통계용 메서드

    /**
     * 팝업별 상태별 예약 목록 조회 (시간대별 통계용)
     */
    List<Reservation> findByPopupAndStatus(Popup popup, ReservationStatus status);

    /**
     * 사용자 ID로 예약 이력 조회 (최신순)
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId ORDER BY r.reservationDate DESC")
    List<Reservation> findByUserIdOrderByReservationDateDesc(@Param("userId") Long userId);

    /**
     * 사용자의 방문 완료된 예약 이력 조회
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.status = 'VISITED' ORDER BY r.reservationDate DESC")
    List<Reservation> findVisitedReservationsByUserId(@Param("userId") Long userId);

    /**
     * 특정 카테고리의 예약 이력 조회
     */
    @Query("SELECT r FROM Reservation r JOIN r.popup p WHERE r.user.id = :userId AND p.category.id = :categoryId ORDER BY r.reservationDate DESC")
    List<Reservation> findByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
}