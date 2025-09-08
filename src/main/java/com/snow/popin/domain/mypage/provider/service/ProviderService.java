package com.snow.popin.domain.mypage.provider.service;

import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.space.repository.SpaceRepository;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationListResponseDto;
import com.snow.popin.domain.spacereservation.dto.SpaceReservationResponseDto;
import com.snow.popin.domain.spacereservation.entity.SpaceReservation;
import com.snow.popin.domain.spacereservation.repository.SpaceReservationRepository;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProviderService {

    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final SpaceReservationRepository srRepository;

    // '공간대여'에서 등록한 내 공간 리스트
    public List<Space> findMySpaces(String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return spaceRepository.findByOwner(owner);
    }

    // 내 공간에 신청된 예약 목록 조회
    public List<SpaceReservationListResponseDto> getReservationsToMySpaces(String email) {
        User provider = getUserByEmail(email);
        log.info("reservations for provider spaces: {}", provider.getId());

        return srRepository.findBySpaceOwnerOrderByCreatedAtDesc(provider)
                .stream()
                .map(SpaceReservationListResponseDto::fromForProvider)
                .collect(Collectors.toList());
    }

    // 예약 상세 조회
    public SpaceReservationResponseDto getReservationDetail(String email, Long reservationId) {
        User provider = getUserByEmail(email);

        SpaceReservation reservation = srRepository.findByIdAndSpaceOwner(reservationId, provider)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 조회 권한이 없습니다."));

        return SpaceReservationResponseDto.from(reservation);
    }

    // 예약 승인
    @Transactional
    public void acceptReservation(String email, Long reservationId) {
        User provider = getUserByEmail(email);
        log.info("Accepting reservation {} by provider {}", reservationId, provider.getId());

        SpaceReservation reservation = srRepository.findByIdAndSpaceOwner(reservationId, provider)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 승인 권한이 없습니다."));

        // 날짜 중복 재확인
        long overlappingCount = srRepository.countOverlappingReservations(
                reservation.getSpace(), reservation.getStartDate(), reservation.getEndDate());
        if (overlappingCount > 0) {
            throw new IllegalArgumentException("해당 기간에 이미 승인된 예약이 있습니다.");
        }

        reservation.accept();
        log.info("Reservation {} accepted successfully", reservationId);
    }

    // 예약 거절
    @Transactional
    public void rejectReservation(String email, Long reservationId) {
        User provider = getUserByEmail(email);
        log.info("Rejecting reservation {} by provider {}", reservationId, provider.getId());

        SpaceReservation reservation = srRepository.findByIdAndSpaceOwner(reservationId, provider)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않거나 거절 권한이 없습니다."));

        reservation.reject();
        log.info("Reservation {} rejected successfully", reservationId);
    }

    // 예약 현황 통계 추가

    // 이메일로 사용자 조회
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
