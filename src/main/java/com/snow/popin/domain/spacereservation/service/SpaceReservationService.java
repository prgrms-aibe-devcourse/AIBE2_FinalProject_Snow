package com.snow.popin.domain.spacereservation.service;

import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.space.repository.SpaceRepository;
import com.snow.popin.domain.spacereservation.dto.*;
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

    // 예약 요청 생성
    public Long createReservation(User host, SpaceReservationCreateRequestDto dto) {
        log.info("Creating reservation for space {} by host {}", dto.getSpaceId(), host.getId());

        // HOST 권한 확인
        if (host.getRole() != Role.HOST) {
            throw new IllegalArgumentException("HOST 권한이 필요합니다.");
        }

        // 공간 조회
        Space space = spaceRepository.findById(dto.getSpaceId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

        // 본인 공간 예약 금지
        if (space.getOwner().getId().equals(host.getId())) {
            throw new IllegalArgumentException("본인 공간은 예약할 수 없습니다.");
        }

        // 공간 이용 가능 기간 확인
        if (dto.getStartDate().isBefore(space.getStartDate()) ||
                dto.getEndDate().isAfter(space.getEndDate())) {
            throw new IllegalArgumentException("공간 이용 가능 기간을 벗어났습니다.");
        }

        // 날짜 중복 확인 (승인된 예약과 겹치는지)
        long overlappingCount = reservationRepository.countOverlappingReservations(
                space, dto.getStartDate(), dto.getEndDate());
        if (overlappingCount > 0) {
            throw new IllegalArgumentException("해당 기간에 이미 승인된 예약이 있습니다.");
        }

        // 예약 생성
        SpaceReservation reservation = SpaceReservation.builder()
                .space(space)
                .host(host)
                .brand(dto.getBrand())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .message(dto.getMessage())
                .contactPhone(dto.getContactPhone())
                .popupDescription(dto.getPopupDescription())
                .status(ReservationStatus.PENDING)
                .build();

        SpaceReservation saved = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {}", saved.getId());

        return saved.getId();
    }

    // HOST가 신청한 예약 목록 조회
    @Transactional(readOnly = true)
    public List<SpaceReservationListResponseDto> getMyRequests(User host) {
        log.info("Fetching reservations for host: {}", host.getId());

        return reservationRepository.findByHostOrderByCreatedAtDesc(host)
                .stream()
                .map(SpaceReservationListResponseDto::fromForHost)
                .collect(Collectors.toList());
    }

    // PROVIDER 에게 신청된 예약 목록 조회
    @Transactional(readOnly = true)
    public List<SpaceReservationListResponseDto> getMySpaceReservations(User provider) {
        log.info("Fetching space reservations for provider: {}", provider.getId());

        return reservationRepository.findBySpaceOwnerOrderByCreatedAtDesc(provider)
                .stream()
                .map(SpaceReservationListResponseDto::fromForProvider)
                .collect(Collectors.toList());
    }

    // 예약 상세 조회
    @Transactional(readOnly = true)
    public SpaceReservationResponseDto getReservationDetail(User user, Long reservationId) {
        SpaceReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // 권한 확인 (예약자이거나 공간 소유자인지)
        if (!reservation.isOwner(user) && !reservation.isSpaceOwner(user)) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }

        return SpaceReservationResponseDto.from(reservation);
    }

    // 예약 승인 (PROVIDER만 가능)
    public void acceptReservation(User provider, Long reservationId) {
        log.info("Accepting reservation {} by provider {}", reservationId, provider.getId());

        SpaceReservation reservation = reservationRepository.findByIdAndSpaceOwner(reservationId, provider)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 승인 권한이 없습니다."));

        // 날짜 중복 재확인 (동시성 문제 방지)
        long overlappingCount = reservationRepository.countOverlappingReservations(
                reservation.getSpace(), reservation.getStartDate(), reservation.getEndDate());
        if (overlappingCount > 0) {
            throw new IllegalArgumentException("해당 기간에 이미 승인된 예약이 있습니다.");
        }

        reservation.accept();
        log.info("Reservation {} accepted successfully", reservationId);
    }

    // 예약 거절 (PROVIDER만 가능)
    public void rejectReservation(User provider, Long reservationId) {
        log.info("Rejecting reservation {} by provider {}", reservationId, provider.getId());

        SpaceReservation reservation = reservationRepository.findByIdAndSpaceOwner(reservationId, provider)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 거절 권한이 없습니다."));

        reservation.reject();
        log.info("Reservation {} rejected successfully", reservationId);
    }

    // 예약 취소 (HOST만 가능)
    public void cancelReservation(User host, Long reservationId) {
        log.info("Cancelling reservation {} by host {}", reservationId, host.getId());

        SpaceReservation reservation = reservationRepository.findByIdAndHost(reservationId, host)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 취소 권한이 없습니다."));

        reservation.cancel();
        log.info("Reservation {} cancelled successfully", reservationId);
    }
}