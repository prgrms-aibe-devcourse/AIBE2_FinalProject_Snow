package com.snow.popin.domain.mypage.host.service;

import com.snow.popin.domain.category.entity.Category;
import com.snow.popin.domain.category.repository.CategoryRepository;
import com.snow.popin.domain.mypage.host.dto.HostProfileResponseDto;
import com.snow.popin.domain.mypage.host.dto.PopupRegisterRequestDto;
import com.snow.popin.domain.mypage.host.dto.PopupRegisterResponseDto;
import com.snow.popin.domain.mypage.host.entity.Host;
import com.snow.popin.domain.mypage.host.repository.HostRepository;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupHours;
import com.snow.popin.domain.popup.entity.Tag;
import com.snow.popin.domain.popup.repository.PopupHoursRepository;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.popup.repository.TagRepository;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.constant.ErrorCode;
import com.snow.popin.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 호스트 서비스
 *
 * - 팝업 등록/수정/삭제
 * - 내가 등록한 팝업 목록/상세 조회
 * - 호스트 프로필 조회
 */
@Service
@RequiredArgsConstructor
public class HostService {

    private final HostRepository hostRepository;
    private final PopupRepository popupRepository;
    private final PopupHoursRepository popupHoursRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    /**
     * 팝업 등록
     *
     * @param user 현재 로그인 사용자
     * @param dto 팝업 등록 요청 DTO
     * @return 생성된 팝업 ID
     */
    @Transactional
    public Long createPopup(User user, PopupRegisterRequestDto dto) {
        Host host = hostRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.UNAUTHORIZED));

        Popup popup = Popup.create(host.getBrand().getId(), dto);

        //  카테고리 매핑
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND));
            popup.setCategory(category);
        }

        //  태그 매핑
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(dto.getTagIds());
            popup.getTags().addAll(tags);
        }

        popupRepository.save(popup);

        // 운영시간 저장
        if (dto.getHours() != null && !dto.getHours().isEmpty()) {
            List<PopupHours> hours = dto.getHours().stream()
                    .map(hourDto -> PopupHours.create(popup, hourDto))
                    .collect(Collectors.toList());
            popupHoursRepository.saveAll(hours);
        }

        return popup.getId();
    }
    /**
     * 내가 등록한 팝업 목록 조회
     *
     * @param user 현재 로그인 사용자
     * @return 팝업 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public Page<PopupRegisterResponseDto> getMyPopups(User user, Pageable pageable) {
        Host host = hostRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.UNAUTHORIZED));

        return popupRepository.findByBrandId(host.getBrand().getId(), pageable)
                .map(PopupRegisterResponseDto::fromEntity);
    }
    /**
     * 내가 등록한 팝업 상세 조회
     *
     * @param user 현재 로그인 사용자
     * @param id 팝업 ID
     * @return 팝업 응답 DTO
     */
    @Transactional(readOnly = true)
    public PopupRegisterResponseDto getMyPopupDetail(User user, Long id) {
        Host host = hostRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.UNAUTHORIZED));

        Popup popup = popupRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND));

        if (!popup.getBrandId().equals(host.getBrand().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        return PopupRegisterResponseDto.fromEntity(popup);
    }
    /**
     * 팝업 수정
     *
     * @param user 현재 로그인 사용자
     * @param id 팝업 ID
     * @param dto 팝업 수정 요청 DTO
     */
    @Transactional
    public void updatePopup(User user, Long id, PopupRegisterRequestDto dto) {
        Host host = hostRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.UNAUTHORIZED));

        Popup popup = popupRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND));

        // 권한 체크
        if (!popup.getBrandId().equals(host.getBrand().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        // 기본 필드 업데이트
        popup.update(dto);

        //  카테고리 업데이트
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND));
            popup.setCategory(category);
        }

        //  태그 업데이트
        popup.getTags().clear();
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(dto.getTagIds());
            popup.getTags().addAll(tags);
        }
    }

    /**
     * 팝업 삭제
     *
     * @param user 현재 로그인 사용자
     * @param id 팝업 ID
     */
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
    /**
     * 내 호스트 프로필 조회
     *
     * @param user 현재 로그인 사용자
     * @return 호스트 프로필 응답 DTO
     */
    @Transactional(readOnly = true)
    public HostProfileResponseDto getMyHostProfile(User user) {
        Host host = hostRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.UNAUTHORIZED));

        return HostProfileResponseDto.from(host);
    }
}
