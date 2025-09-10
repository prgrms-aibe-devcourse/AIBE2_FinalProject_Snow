package com.snow.popin.domain.popupReservation.service;

import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.mypage.host.repository.BrandRepository;
import com.snow.popin.domain.mypage.host.repository.HostRepository;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.popupReservation.dto.ReservationRequestDto;
import com.snow.popin.domain.popupReservation.dto.ReservationResponseDto;
import com.snow.popin.domain.popupReservation.entity.Reservation;
import com.snow.popin.domain.popupReservation.repository.ReservationRepository;
import com.snow.popin.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PopupRepository popupRepository;
    private final HostRepository hostRepository;
    private final BrandRepository brandRepository;

    // 예약하기
    @Transactional
    public Long createReservation(User currentUser, Long popupId, ReservationRequestDto dto) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팝업이 존재하지 않습니다."));

        if (!popup.getReservationAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 팝업은 예약을 받을 수 없습니다.");
        }

        boolean exists = reservationRepository.existsByPopupAndUser(popup, currentUser);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 예약한 팝업입니다.");
        }

        Reservation reservation = Reservation.create(
                popup,
                currentUser,
                dto.getName(),
                dto.getPhone(),
                dto.getReservationDate()
        );
        reservationRepository.save(reservation);

        return reservation.getId();
    }

    // 팝업 예약 현황 조회 (브랜드 멤버만 가능)
    public List<ReservationResponseDto> getPopupReservations(Long popupId, User currentUser) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팝업이 존재하지 않습니다."));

        Brand brand = brandRepository.findById(popup.getBrandId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "브랜드가 존재하지 않습니다."));

        boolean isMember = hostRepository.existsByBrandAndUser(brand, currentUser);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        return reservationRepository.findByPopup(popup)
                .stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<ReservationResponseDto> getMyReservations(User currentUser) {
        return reservationRepository.findByUser(currentUser)
                .stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservation(Long reservationId, User currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."));

        if (!reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }
        reservation.cancel();
    }

    //  방문 완료 처리
    @Transactional
    public void markAsVisited(Long reservationId, User currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."));

        Brand brand = brandRepository.findById(reservation.getPopup().getBrandId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "브랜드가 존재하지 않습니다."));

        boolean isMember = hostRepository.existsByBrandAndUser(brand, currentUser);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        reservation.markAsVisited();
    }
}
