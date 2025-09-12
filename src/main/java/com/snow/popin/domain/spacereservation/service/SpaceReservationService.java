package com.snow.popin.domain.spacereservation.service;

import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.mypage.host.entity.Host;
import com.snow.popin.domain.mypage.host.repository.HostRepository;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.space.repository.SpaceRepository;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationCreateRequestDto;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationListResponseDto;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationResponseDto;
import com.snow.popin.domain.spacereservation.entity.SpaceReservation;
import com.snow.popin.domain.spacereservation.entity.ReservationStatus;
import com.snow.popin.domain.spacereservation.repository.SpaceReservationRepository;
import com.snow.popin.domain.user.constant.Role;
import com.snow.popin.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SpaceReservationService {

    private final SpaceReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final PopupRepository popupRepository;
    private final HostRepository hostRepository;

    /**
     * 공간 예약 생성
     *
     * @param user HOST 권한 사용자
     * @param dto  예약 요청 DTO (spaceId, popupId)
     * @return 생성된 예약 ID
     */
    @Transactional
    public Long createReservation(User user, SpaceReservationCreateRequestDto dto) {
        log.info("DTO start={}, end={}", dto.getStartDate(), dto.getEndDate());

        Host hostEntity = hostRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("호스트 정보가 없습니다."));

        Brand brand = hostEntity.getBrand();

        Space space = spaceRepository.findById(dto.getSpaceId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

        Popup popup = popupRepository.findById(dto.getPopupId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팝업입니다."));

        // 예약 엔티티 생성
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

        log.info("Entity start={}, end={}, brand={}",
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getBrand().getName());

        return reservationRepository.save(reservation).getId();
    }

    /**
     * HOST가 신청한 예약 목록 조회
     */
    @Transactional(readOnly = true)
    public List<SpaceReservationListResponseDto> getMyRequests(User host) {
        log.info("Fetching reservations for host: {}", host.getId());

        return reservationRepository.findByHostOrderByCreatedAtDesc(host)
                .stream()
                .map(SpaceReservationListResponseDto::fromForHost)
                .collect(Collectors.toList());
    }

    /**
     * PROVIDER의 공간에 신청된 예약 목록 조회
     */
    @Transactional(readOnly = true)
    public List<SpaceReservationListResponseDto> getMySpaceReservations(User provider) {
        log.info("Fetching space reservations for provider: {}", provider.getId());

        return reservationRepository.findBySpaceOwnerOrderByCreatedAtDesc(provider)
                .stream()
                .map(SpaceReservationListResponseDto::fromForProvider)
                .collect(Collectors.toList());
    }

    /**
     * 예약 상세 조회
     */
    @Transactional(readOnly = true)
    public SpaceReservationResponseDto getReservationDetail(User user, Long reservationId) {
        SpaceReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (!reservation.isOwner(user) && !reservation.isSpaceOwner(user)) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }

        return SpaceReservationResponseDto.from(reservation);
    }

    /**
     * 예약 승인 (PROVIDER만 가능)
     */
    public void acceptReservation(User provider, Long reservationId) {
        log.info("Accepting reservation {} by provider {}", reservationId, provider);

        SpaceReservation reservation = reservationRepository.findByIdAndSpaceOwner(reservationId, provider)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 승인 권한이 없습니다."));

        reservation.accept();
        log.info("Reservation {} accepted successfully", reservationId);
    }

    /**
     * 예약 거절 (PROVIDER만 가능)
     */
    public void rejectReservation(User provider, Long reservationId) {
        log.info("Rejecting reservation {} by provider {}", reservationId, provider);

        SpaceReservation reservation = reservationRepository.findByIdAndSpaceOwner(reservationId, provider)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 거절 권한이 없습니다."));

        reservation.reject();
        log.info("Reservation {} rejected successfully", reservationId);
    }

    /**
     * 예약 취소 (HOST만 가능)
     */
    public void cancelReservation(User host, Long reservationId) {
        log.info("Cancelling reservation {} by host {}", reservationId, host.getId());

        SpaceReservation reservation = reservationRepository.findByIdAndHost(reservationId, host)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 취소 권한이 없습니다."));

        reservation.cancel();
        log.info("Reservation {} cancelled successfully", reservationId);
    }
}
