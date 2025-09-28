package com.snow.popin.domain.space.service;

import com.snow.popin.domain.map.entity.Venue;
import com.snow.popin.domain.map.repository.MapRepository;
import com.snow.popin.domain.space.dto.SpaceCreateRequestDto;
import com.snow.popin.domain.space.dto.SpaceListResponseDto;
import com.snow.popin.domain.space.dto.SpaceResponseDto;
import com.snow.popin.domain.space.dto.SpaceUpdateRequestDto;
import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.space.repository.SpaceRepository;
import com.snow.popin.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SpaceService
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final FileStorageService fileStorageService;
    private final MapRepository venueRepository;

    /**
     * 공간 등록
     *
     * @param owner 공간 등록자 (User)
     * @param dto   공간 등록 요청 DTO
     * @return 생성된 공간 ID
     */
    @Transactional
    public Long create(User owner, SpaceCreateRequestDto dto) {
        log.info("Creating new space for owner: {}", owner.getId());

        Venue venue = Venue.of(
                dto.getTitle(),
                dto.getRoadAddress(),
                dto.getJibunAddress(),
                dto.getDetailAddress(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getParkingAvailable()
        );
        venueRepository.save(venue);

        String imageUrl = fileStorageService.save(dto.getImage());

        Space space = Space.builder()
                .owner(owner)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .areaSize(dto.getAreaSize())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .rentalFee(dto.getRentalFee())
                .contactPhone(dto.getContactPhone())
                .coverImageUrl(imageUrl)
                .venue(venue)
                .build();

        Space saved = spaceRepository.save(space);
        log.info("Space created successfully with ID: {}", saved.getId());
        return saved.getId();
    }

    /**
     * 모든 공개 공간 목록 조회
     *
     * @param me 현재 사용자
     * @return 공개된 공간 리스트
     */
    @Transactional(readOnly = true)
    public List<SpaceListResponseDto> listAll(User me, Pageable pageable) {
        return spaceRepository.findByIsPublicTrueAndIsHiddenFalseOrderByCreatedAtDesc(pageable)
                .stream()
                .map(space -> SpaceListResponseDto.from(space, me))
                .collect(Collectors.toList());
    }

    /**
     * 공간 상세 조회
     *
     * @param me 현재 사용자 (비로그인 허용)
     * @param id 공간 ID
     * @return 공간 상세 응답 DTO
     */
    @Transactional(readOnly = true)
    public SpaceResponseDto getDetail(User me, Long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공간이 존재하지 않습니다."));

        boolean mine = (me != null && space.getOwner().getId().equals(me.getId()));
        if (!Boolean.TRUE.equals(space.getIsPublic()) && !mine) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }
        return SpaceResponseDto.from(space);
    }

    /**
     * 공간 게시글 수정
     *
     * @param owner   수정 요청 사용자
     * @param spaceId 공간 ID
     * @param dto     수정 요청 DTO
     */
    @Transactional
    public void update(User owner, Long spaceId, SpaceUpdateRequestDto dto) {
        log.info("Updating space ID {} by user: {}", spaceId, owner.getId());

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공간이 존재하지 않습니다."));

        if (!space.isOwner(owner)) {
            throw new AccessDeniedException("해당 공간에 대한 수정 권한이 없습니다.");
        }

        Venue venue = space.getVenue();
        if (venue == null) {
            venue = Venue.of(
                    dto.getTitle(),
                    dto.getRoadAddress(),
                    dto.getJibunAddress(),
                    dto.getDetailAddress(),
                    dto.getLatitude(),
                    dto.getLongitude(),
                    dto.getParkingAvailable()
            );
        } else {
            venue.update(
                    dto.getTitle(),
                    dto.getRoadAddress(),
                    dto.getJibunAddress(),
                    dto.getDetailAddress(),
                    dto.getLatitude(),
                    dto.getLongitude(),
                    dto.getParkingAvailable()
            );
        }
        venueRepository.save(venue);

        String imageUrl = space.getCoverImageUrl();
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            imageUrl = fileStorageService.save(dto.getImage());
        }

        space.updateSpaceInfo(
                dto.getTitle(),
                dto.getDescription(),
                dto.getAreaSize(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getRentalFee(),
                dto.getContactPhone()
        );
        space.updateVenue(venue);
        space.updateCoverImage(imageUrl);

        log.info("Space ID {} updated successfully", spaceId);
    }

    /**
     * 공간 게시글 삭제
     *
     * @param owner 삭제 요청 사용자
     * @param id    공간 ID
     */
    public void deleteSpace(User owner, Long id) {
        log.info("Deleting space ID: {} by owner: {}", id, owner.getId());

        Space space = spaceRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("공간이 없거나 삭제 권한이 없습니다."));

        space.hide();
        log.info("Space deleted successfully: {}", id);
    }

    /**
     * 내가 등록한 공간 목록 조회
     *
     * @param owner 사용자
     * @return 내 공간 리스트
     */
    @Transactional(readOnly = true)
    public List<SpaceListResponseDto> listMine(User owner) {
        log.debug("Fetching spaces for owner: {}", owner.getId());

        return spaceRepository.findByOwnerAndIsHiddenFalseOrderByCreatedAtDesc(owner)
                .stream()
                .map(space -> SpaceListResponseDto.from(space, owner))
                .collect(Collectors.toList());
    }

    /**
     * 공간 숨김 처리 (신고 시)
     *
     * @param reporter 신고자
     * @param spaceId  공간 ID
     */
    @Transactional
    public void hideSpace(User reporter, Long spaceId) {
        log.info("Hiding space ID: {} reported by user: {}", spaceId, reporter.getId());

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공간이 존재하지 않습니다."));

        if (space.isOwner(reporter)) {
            throw new IllegalArgumentException("자신의 공간은 신고할 수 없습니다.");
        }

        space.hide();
        log.info("Space ID {} hidden successfully", spaceId);
    }

    /**
     * 공간 검색
     *
     * @param me       현재 사용자
     * @param keyword  검색 키워드 (선택)
     * @param location 위치 (선택)
     * @param minArea  최소 면적 (선택)
     * @param maxArea  최대 면적 (선택)
     * @return 검색 조건에 맞는 공간 리스트
     */
    @Transactional(readOnly = true)
    public List<SpaceListResponseDto> searchSpaces(User me, String keyword, String location,
                                                   Integer minArea, Integer maxArea) {
        log.debug("Searching spaces with filters - keyword: {}, location: {}", keyword, location);

        List<Space> spaces = spaceRepository.searchSpacesWithJoins(keyword, location, minArea, maxArea);

        return spaces.stream()
                .map(space -> SpaceListResponseDto.from(space, me))
                .collect(Collectors.toList());
    }
}
