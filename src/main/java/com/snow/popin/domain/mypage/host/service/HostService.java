package com.snow.popin.domain.mypage.host.service;

import com.snow.popin.domain.mypage.host.dto.HostProfileResponseDto;
import com.snow.popin.domain.mypage.host.dto.PopupRegisterRequestDto;
import com.snow.popin.domain.mypage.host.dto.PopupRegisterResponseDto;
import com.snow.popin.domain.mypage.host.entity.Host;
import com.snow.popin.domain.mypage.host.repository.HostRepository;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.constant.ErrorCode;
import com.snow.popin.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HostService {

    private final HostRepository hostRepository;
    private final PopupRepository popupRepository;

    // 팝업 등록
    @Transactional
    public Long createPopup(User user, PopupRegisterRequestDto dto) {
        // 유저가 Host인지 검증
        Host host = hostRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.UNAUTHORIZED));

        //  팝업 생성
        Popup popup = Popup.create(host.getBrand().getId(), dto);

        popupRepository.save(popup);
        return popup.getId();
    }

    // 내가 등록한 팝업 목록 조회
    @Transactional(readOnly = true)
    public List<PopupRegisterResponseDto> getMyPopups(User user) {
        Host host = hostRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.UNAUTHORIZED));

        return popupRepository.findByBrandId(host.getBrand().getId())
                .stream()
                .map(PopupRegisterResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 팝업 수정
    @Transactional
    public void updatePopup(User user, Long id, PopupRegisterRequestDto dto) {
        Host host = hostRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.UNAUTHORIZED));

        Popup popup = popupRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND));

        if (!popup.getBrandId().equals(host.getBrand().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        popup.update(dto);
    }

    // 팝업 삭제
    @Transactional
    public void deletePopup(User user, Long id) {
        Host host = hostRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.UNAUTHORIZED));

        Popup popup = popupRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND));

        if (!popup.getBrandId().equals(host.getBrand().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        popupRepository.delete(popup);
    }

    // 내 프로필 조회
    @Transactional(readOnly = true)
    public HostProfileResponseDto getMyHostProfile(User user) {
        Host host = hostRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.UNAUTHORIZED));

        return HostProfileResponseDto.from(host);
    }

}
