package com.snow.popin.domain.popupReservation.service;

import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.mypage.host.repository.BrandRepository;
import com.snow.popin.domain.mypage.host.repository.HostRepository;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.popupReservation.dto.PopupCapacitySettingsDto;
import com.snow.popin.domain.popupReservation.entity.PopupReservationSettings;
import com.snow.popin.domain.popupReservation.repository.PopupReservationSettingsRepository;
import com.snow.popin.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupReservationSettingsService {

    private final PopupReservationSettingsRepository settingsRepository;
    private final PopupRepository popupRepository;
    private final BrandRepository brandRepository;
    private final HostRepository hostRepository;

    /**
     * 팝업 예약 설정 조회
     * 설정이 없으면 기본값으로 자동 생성
     */
    @Cacheable(value = "popupReservationSettings", key = "#popupId")
    public PopupReservationSettings getSettings(Long popupId) {
        PopupReservationSettings settings = settingsRepository.findByPopupId(popupId)
                .orElseGet(() -> createDefaultSettings(popupId));

        return applyDefaults(settings);
    }

    /**
     * null 값 보정 (기본값 적용)
     */
    private PopupReservationSettings applyDefaults(PopupReservationSettings settings) {
        if (settings.getTimeSlotInterval() == null) {
            settings.setTimeSlotInterval(30); // 기본 30분
        }
        if (settings.getMaxCapacityPerSlot() == null) {
            settings.setMaxCapacityPerSlot(10); // 기본 10명
        }
        if (settings.getMaxPartySize() == null) {
            settings.setMaxPartySize(5); // 기본 5명
        }
        if (settings.getAdvanceBookingDays() == null) {
            settings.setAdvanceBookingDays(30); // 기본 30일 전까지 예약 가능
        }
        if (settings.getAllowSameDayBooking() == null) {
            settings.setAllowSameDayBooking(true); // 기본 당일 예약 허용
        }
        return settings;
    }

    /**
     * 예약 설정 조회 (DTO 형태, 권한 체크 포함)
     */
    public PopupCapacitySettingsDto getCapacitySettings(Long popupId, User currentUser) {
        validateHostPermission(popupId, currentUser);

        PopupReservationSettings settings = getSettings(popupId);
        return PopupCapacitySettingsDto.from(settings);
    }

    /**
     * 기본 예약 설정 업데이트
     */
    @Transactional
    @CacheEvict(value = "popupReservationSettings", key = "#popupId")
    public void updateBasicSettings(Long popupId, PopupCapacitySettingsDto dto, User currentUser) {
        validateHostPermission(popupId, currentUser);

        PopupReservationSettings settings = getSettings(popupId);
        settings.updateBasicSettings(dto.getMaxCapacityPerSlot(), dto.getTimeSlotInterval());

        settingsRepository.save(settings);

        log.info("팝업 예약 설정 업데이트: popupId={}, maxCapacity={}, timeInterval={}",
                popupId, dto.getMaxCapacityPerSlot(), dto.getTimeSlotInterval());
    }

    /**
     * 기본 설정으로 팝업 예약 설정 생성
     */
    @Transactional
    public PopupReservationSettings createDefaultSettings(Long popupId) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팝업이 존재하지 않습니다."));

        PopupReservationSettings settings = PopupReservationSettings.createDefault(popup);
        // 기본값 보정
        applyDefaults(settings);

        PopupReservationSettings saved = settingsRepository.save(settings);
        log.info("팝업 기본 예약 설정 생성: popupId={}", popupId);
        return saved;
    }

    /**
     * 호스트 권한 검증
     */
    private void validateHostPermission(Long popupId, User currentUser) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팝업이 존재하지 않습니다."));

        Brand brand = brandRepository.findById(popup.getBrandId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "브랜드가 존재하지 않습니다."));

        boolean isMember = hostRepository.existsByBrandAndUser(brand, currentUser.getId());
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "예약 설정을 변경할 권한이 없습니다.");
        }
    }
}
