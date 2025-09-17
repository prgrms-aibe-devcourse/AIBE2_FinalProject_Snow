package com.snow.popin.domain.popupReservation.service;

import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.mypage.host.repository.BrandRepository;
import com.snow.popin.domain.mypage.host.repository.HostRepository;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupHours;
import com.snow.popin.domain.popup.repository.PopupHoursRepository;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.popupReservation.dto.ReservationRequestDto;
import com.snow.popin.domain.popupReservation.dto.ReservationResponseDto;
import com.snow.popin.domain.popupReservation.dto.TimeSlotDto;
import com.snow.popin.domain.popupReservation.entity.Reservation;
import com.snow.popin.domain.popupReservation.repository.ReservationRepository;
import com.snow.popin.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 팝업 예약 관련 서비스
 *
 * - 예약 생성
 * - 팝업별 예약 현황 조회
 * - 내 예약 조회
 * - 예약 취소
 * - 방문 완료 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PopupRepository popupRepository;
    private final HostRepository hostRepository;
    private final BrandRepository brandRepository;
    private final PopupHoursRepository popupHoursRepository;

    // TODO: popup 등록 시 최대 예약 가능 인원 등록
    // 시간 슬롯당 최대 예약 가능 인원
    private static final int DEFAULT_MAX_CAPACITY_PER_SLOT = 10;

    // 시간 슬롯 간격 (분)
    private static final int TIME_SLOT_INTERVAL_MINUTES = 30;

    /**
     * 팝업 예약 생성
     *
     * @param currentUser 현재 사용자
     * @param popupId     예약할 팝업 ID
     * @param dto         예약 요청 DTO
     */
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

        if (dto.getReservationDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "예약 일시를 선택해 주세요.");
        }
        if (dto.getPartySize() == null || dto.getPartySize() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "예약 인원을 1명 이상 입력해 주세요.");
        }

        LocalDate reservationDate = dto.getReservationDate().toLocalDate();
        LocalTime reservationTime = dto.getReservationDate().toLocalTime();

        List<TimeSlotDto> availableSlots = getAvailableTimeSlots(popupId, reservationDate);
        TimeSlotDto matchedSlot = availableSlots.stream()
                .filter(slot -> slot.getStartTime().equals(reservationTime))
                .findFirst()
                .orElse(null);

        if (matchedSlot == null || !matchedSlot.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 시간은 예약이 불가능합니다.");
        }
        if (dto.getPartySize() > matchedSlot.getRemainingSlots()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잔여 인원보다 많은 인원은 예약할 수 없습니다.");
        }

        Reservation reservation = Reservation.create(
                popup,
                currentUser,
                dto.getName(),
                dto.getPhone(),
                dto.getPartySize(),
                dto.getReservationDate()
        );
        reservationRepository.save(reservation);

        return reservation.getId();
    }

    /**
     * 특정 팝업의 예약 현황 조회
     * (브랜드 멤버만 가능)
     *
     * @param popupId     팝업 ID
     * @param currentUser 현재 사용자
     * @return 예약 응답 DTO 리스트
     */
    public List<ReservationResponseDto> getPopupReservations(Long popupId, User currentUser) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팝업이 존재하지 않습니다."));

        Brand brand = brandRepository.findById(popup.getBrandId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "브랜드가 존재하지 않습니다."));

        boolean isMember = hostRepository.existsByBrandAndUser(brand, currentUser.getId());
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        return reservationRepository.findByPopup(popup)
                .stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 현재 사용자의 예약 목록 조회
     *
     * @param currentUser 현재 사용자
     * @return 예약 응답 DTO 리스트
     */
    public List<ReservationResponseDto> getMyReservations(User currentUser) {
        return reservationRepository.findByUser(currentUser)
                .stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 예약 취소
     *
     * @param reservationId 예약 ID
     * @param currentUser   현재 사용자
     */
    @Transactional
    public void cancelReservation(Long reservationId, User currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."));

        if (!reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }
        reservation.cancel();
    }

    /**
     * 예약을 방문 완료 처리
     *
     * @param reservationId 예약 ID
     * @param currentUser   현재 사용자
     */
    @Transactional
    public void markAsVisited(Long reservationId, User currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."));

        Brand brand = brandRepository.findById(reservation.getPopup().getBrandId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "브랜드가 존재하지 않습니다."));

        boolean isMember = hostRepository.existsByBrandAndUser(brand, currentUser.getId());
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        reservation.markAsVisited();
    }

    // 특정 팝업의 예약 가능한 날짜 목록 조회
    public List<LocalDate> getAvailableDates(Long popupId) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팝업이 존재하지 않습니다."));

        if (!popup.getReservationAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 팝업은 예약을 받을 수 없습니다.");
        }

        // 팝업 운영 기간 내에서 예약 가능한 날짜 계산
        LocalDate startDate = popup.getStartDate() != null ? popup.getStartDate() : LocalDate.now();
        LocalDate endDate = popup.getEndDate() != null ? popup.getEndDate() : LocalDate.now().plusMonths(3);

        // 오늘보다 이전 날짜는 제외
        if (startDate.isBefore(LocalDate.now())) {
            startDate = LocalDate.now();
        }

        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            // 해당 날짜에 운영 시간이 있는지 확인
            if (hasOperatingHours(popupId, current)) {
                availableDates.add(current);
            }
            current = current.plusDays(1);
        }

        return availableDates;
    }

    // 특정 팝업의 특정 날짜에 예약 가능한 시간 슬롯 조회
    public List<TimeSlotDto> getAvailableTimeSlots(Long popupId, LocalDate date) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팝업이 존재하지 않습니다."));

        if (!popup.getReservationAvailable()) {
            return new ArrayList<>();
        }

        // 해당 날짜의 운영 시간 조회
        List<PopupHours> operatingHours = popupHoursRepository.findByPopupIdAndDayOfWeek(
                popupId, date.getDayOfWeek().getValue() % 7
        );

        if (operatingHours.isEmpty()) {
            return new ArrayList<>();
        }

        List<TimeSlotDto> timeSlots = new ArrayList<>();

        for (PopupHours hours : operatingHours) {
            LocalTime startTime = hours.getOpenTime();
            LocalTime endTime = hours.getCloseTime();

            // 30분 간격으로 시간 슬롯 생성
            LocalTime currentTime = startTime;
            while (currentTime.isBefore(endTime)) {
                LocalTime slotEndTime = currentTime.plusMinutes(TIME_SLOT_INTERVAL_MINUTES);
                if (slotEndTime.isAfter(endTime)) {
                    break;
                }

                // 해당 시간대의 예약 수 확인
                LocalDateTime slotStart = LocalDateTime.of(date, currentTime);
                LocalDateTime slotEnd = LocalDateTime.of(date, slotEndTime);

                long totalReservedPeople = reservationRepository.sumPartySizeByPopupAndReservationDateBetween(
                        popup, slotStart, slotEnd
                );

                int remainingSlots = DEFAULT_MAX_CAPACITY_PER_SLOT - (int) totalReservedPeople;
                boolean isAvailable = remainingSlots > 0 && !slotStart.isBefore(LocalDateTime.now());

                TimeSlotDto slot = TimeSlotDto.builder()
                        .startTime(currentTime)
                        .endTime(slotEndTime)
                        .timeRangeText(formatTimeRange(currentTime, slotEndTime))
                        .available(isAvailable)
                        .remainingSlots(Math.max(0, remainingSlots))
                        .build();

                timeSlots.add(slot);
                currentTime = currentTime.plusMinutes(TIME_SLOT_INTERVAL_MINUTES);
            }
        }

        return timeSlots;
    }

    // 특정 날짜에 운영 시간이 있는지 확인
    private boolean hasOperatingHours(Long popupId, LocalDate date) {
        List<PopupHours> hours = popupHoursRepository.findByPopupIdAndDayOfWeek(
                popupId, date.getDayOfWeek().getValue() % 7
        );
        return !hours.isEmpty();
    }

    // 시간 범위를 문자열로 포맷팅
    private String formatTimeRange(LocalTime startTime, LocalTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return startTime.format(formatter) + " - " + endTime.format(formatter);
    }
}