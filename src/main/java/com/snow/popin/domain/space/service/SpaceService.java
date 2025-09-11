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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final FileStorageService fileStorageService;
    private final MapRepository venueRepository;

    //공간 등록
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

    //모든 공간 목록 조회
    @Transactional(readOnly = true)
    public List<SpaceListResponseDto> listAll(User me) {
        return spaceRepository.findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(space -> SpaceListResponseDto.from(space, me))
                .collect(Collectors.toList());
    }

    //공간 상세보기
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

    //공간 게시글 수정
    @Transactional
    public void update(User owner, Long spaceId, SpaceUpdateRequestDto dto) {
        log.info("Updating space ID {} by user: {}", spaceId, owner.getId());

        // 1) 기존 Space 조회 및 소유자 검증
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공간이 존재하지 않습니다."));

        if (!space.isOwner(owner)) {
            throw new AccessDeniedException("해당 공간에 대한 수정 권한이 없습니다.");
        }

        // 2) Venue 수정 또는 새로 생성
        Venue venue = space.getVenue();
        if (venue == null) {
            venue = Venue.of(
                    dto.getTitle(),  // Venue.name = 공간 제목과 동일
                    dto.getRoadAddress(),
                    dto.getJibunAddress(),
                    dto.getDetailAddress(),
                    dto.getLatitude(),
                    dto.getLongitude(),
                    dto.getParkingAvailable()  // ← 필요 시 null 허용 or 기본값 처리
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

        // 3) 이미지 업로드 (있을 경우만)
        String imageUrl = space.getCoverImageUrl();
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            imageUrl = fileStorageService.save(dto.getImage());
        }

        // 4) Space 엔티티 값 업데이트
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


    //공간 게시글 삭제

    public void delete(User owner, Long id) {
        log.info("Deleting space ID: {} by owner: {}", id, owner.getId());

        Space space = spaceRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("공간이 없거나 삭제 권한이 없습니다."));

        spaceRepository.delete(space);
        log.info("Space deleted successfully: {}", id);
    }

    //내가 등록한 공간 목록 조회
    @Transactional(readOnly = true)
    public List<SpaceListResponseDto> listMine(User owner) {
        log.debug("Fetching spaces for owner: {}", owner.getId());

        return spaceRepository.findByOwnerOrderByCreatedAtDesc(owner)
                .stream()
                .map(space -> SpaceListResponseDto.from(space, owner))
                .collect(Collectors.toList());
    }


}
