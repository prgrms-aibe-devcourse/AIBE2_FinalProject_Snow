package com.snow.popin.domain.spacereservation.service;

import com.snow.popin.domain.map.entity.Venue;
import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.mypage.host.entity.Host;
import com.snow.popin.domain.mypage.host.repository.HostRepository;
import com.snow.popin.domain.notification.constant.NotificationType;
import com.snow.popin.domain.notification.service.NotificationService;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.space.repository.SpaceRepository;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationCreateRequestDto;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationListResponseDto;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationResponseDto;
import com.snow.popin.domain.spacereservation.entity.SpaceReservation;
import com.snow.popin.domain.spacereservation.entity.ReservationStatus;
import com.snow.popin.domain.spacereservation.repository.SpaceReservationRepository;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SpaceReservationService
 * 공간 예약 관련 비즈니스 로직 처리
 * - 예약 생성, 조회, 승인/거절, 취소, 삭제
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SpaceReservationService {

    private final SpaceReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final PopupRepository popupRepository;
    private final HostRepository hostRepository;
    private final UserUtil userUtil;
    private final NotificationService notificationService;

    /**
     * 공간 예약 생성 (HOST)
     *
     * @param dto 예약 생성 요청 DTO
     * @return 생성된 예약 ID
     */
    @Transactional
    public Long createReservation(SpaceReservationCreateRequestDto dto) {
        User user = userUtil.getCurrentUser();
        log.info("DTO start={}, end={}", dto.getStartDate(), dto.getEndDate());

        Host hostEntity = hostRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("호스트 정보가 없습니다."));
        Brand brand = hostEntity.getBrand();

        Space space = spaceRepository.findById(dto.getSpaceId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));
        Popup popup = popupRepository.findById(dto.getPopupId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팝업입니다."));

        SpaceReservation reservation = SpaceReservation.builder()
                .space(space)
                .host(user)
                .popup(popup)
                .brand(brand)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .message(dto.getMessage())
                .contactPhone(dto.getContactPhone())
                .status(ReservationStatus.PENDING)
                .build();

        SpaceReservation saved = reservationRepository.save(reservation);

        // 공간 소유자에게 알림 전송
        notificationService.createNotification(
                space.getOwner().getId(),
                "새로운 공간 예약 신청",
                String.format("%s님이 '%s' 공간에 예약을 신청했습니다.",
                        user.getName(), space.getTitle()),
                NotificationType.RESERVATION,
                "/provider/reservations/" + saved.getId()
        );

        log.info("예약 생성 완료 및 알림 전송: 예약ID={}, 공간소유자={}", saved.getId(), space.getOwner().getEmail());

        return saved.getId();
    }

    /**
     * 내가 신청한 예약 목록 조회 (HOST)
     *
     * @return 예약 목록
     */
    @Transactional(readOnly = true)
    public List<SpaceReservationListResponseDto> getMyRequests() {
        User host = userUtil.getCurrentUser();
        return reservationRepository.findByHostOrderByCreatedAtDesc(host)
                .stream()
                .map(SpaceReservationListResponseDto::fromForHost)
                .collect(Collectors.toList());
    }

    /**
     * 내 공간에 신청된 예약 목록 조회 (PROVIDER)
     *
     * @return 예약 목록
     */
    @Transactional(readOnly = true)
    public List<SpaceReservationListResponseDto> getMySpaceReservations() {
        User provider = userUtil.getCurrentUser();
        return reservationRepository.findBySpaceOwnerOrderByCreatedAtDesc(provider)
                .stream()
                .map(SpaceReservationListResponseDto::fromForProvider)
                .collect(Collectors.toList());
    }

    /**
     * 예약 상세 조회
     *
     * @param reservationId 예약 ID
     * @return 예약 상세 응답 DTO
     */
    @Transactional(readOnly = true)
    public SpaceReservationResponseDto getReservationDetail(Long reservationId) {
        User user = userUtil.getCurrentUser();
        SpaceReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (!reservation.isOwner(user) && !reservation.isSpaceOwner(user)) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }
        return SpaceReservationResponseDto.from(reservation);
    }

    /**
     * 예약 승인 (PROVIDER)
     *
     * @param reservationId 예약 ID
     */
    @Transactional
    public void acceptReservation(Long reservationId) {
        User currentUser = userUtil.getCurrentUser();
        SpaceReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (!reservation.getSpace().getOwner().equals(currentUser)) {
            throw new IllegalArgumentException("해당 공간에 대한 승인 권한이 없습니다.");
        }

        reservation.accept();

        // 예약 신청자에게 알림 전송
        notificationService.createNotification(
                reservation.getHost().getId(),
                "공간 예약 승인",
                String.format("'%s' 공간 예약이 승인되었습니다!",
                        reservation.getSpace().getTitle()),
                NotificationType.RESERVATION,
                "/host/reservations/" + reservationId
        );

        Popup popup = reservation.getPopup();
        if (popup != null && reservation.getSpace() != null) {
            Venue venue = reservation.getSpace().getVenue();
            if (venue != null) {
                popup.setVenue(venue);
                popup.setStatus(PopupStatus.ONGOING);
            }
        }

        log.info("예약 승인 완료 및 알림 전송: 예약ID={}, 호스트={}", reservationId, reservation.getHost().getEmail());
    }

    /**
     * 예약 거절 (PROVIDER)
     *
     * @param reservationId 예약 ID
     */
    public void rejectReservation(Long reservationId) {
        User provider = userUtil.getCurrentUser();
        SpaceReservation reservation = reservationRepository.findByIdAndSpaceOwner(reservationId, provider)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 거절 권한이 없습니다."));

        reservation.reject();

        // 예약 신청자에게 알림 전송
        notificationService.createNotification(
                reservation.getHost().getId(),
                "공간 예약 거절",
                String.format("'%s' 공간 예약이 거절되었습니다.",
                        reservation.getSpace().getTitle()),
                NotificationType.RESERVATION,
                "/host/reservations/" + reservationId
        );

        log.info("예약 거절 완료 및 알림 전송: 예약ID={}, 호스트={}", reservationId, reservation.getHost().getEmail());
    }

    /**
     * 예약 취소 (HOST)
     *
     * @param reservationId 예약 ID
     */
    public void cancelReservation(Long reservationId) {
        User host = userUtil.getCurrentUser();
        SpaceReservation reservation = reservationRepository.findByIdAndHost(reservationId, host)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 취소 권한이 없습니다."));

        reservation.cancel();

        // 공간 소유자에게 알림 전송
        notificationService.createNotification(
                reservation.getSpace().getOwner().getId(),
                "공간 예약 취소",
                String.format("%s님이 '%s' 공간 예약을 취소했습니다.",
                        host.getName(), reservation.getSpace().getTitle()),
                NotificationType.RESERVATION,
                "/provider/reservations/" + reservationId
        );

        log.info("예약 취소 완료 및 알림 전송: 예약ID={}, 공간소유자={}", reservationId, reservation.getSpace().getOwner().getEmail());
    }

    /**
     * 예약 삭제 (거절된 예약만 가능, PROVIDER)
     *
     * @param reservationId 예약 ID
     */
    public void deleteReservation(Long reservationId) {
        User provider = userUtil.getCurrentUser();
        SpaceReservation reservation = reservationRepository.findByIdAndSpaceOwner(reservationId, provider)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 삭제 권한이 없습니다."));

        if (reservation.getStatus() == ReservationStatus.REJECTED) {
            reservationRepository.delete(reservation);
            log.info("Reservation {} deleted by provider {}", reservationId, provider.getEmail());
        } else {
            throw new IllegalArgumentException("승인되었거나 취소된 예약은 삭제할 수 없습니다.");
        }
    }
}