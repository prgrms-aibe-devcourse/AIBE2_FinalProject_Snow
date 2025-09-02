package com.example.popin.domain.space.service;

import com.example.popin.domain.space.dto.*;
import com.example.popin.domain.space.entity.Space;
import com.example.popin.domain.space.repository.SpaceRepository;
import com.example.popin.domain.user.User;
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
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final FileStorageService fileStorageService;

    //공간 등록
    public Long create(User owner, SpaceCreateRequestDto dto) {
        log.info("Creating new space for owner: {}", owner.getId());

        // 이미지 업로드 처리
        String imageUrl = fileStorageService.save(dto.getImage());

        // Entity 생성
        Space space = Space.builder()
                .owner(owner)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .address(dto.getAddress())
                .areaSize(dto.getAreaSize())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .rentalFee(dto.getRentalFee())
                .contactPhone(dto.getContactPhone())
                .coverImageUrl(imageUrl)
                .build();

        // 저장
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

    //공간 게시글 수정
    @Transactional
    public void update(User owner, Long id, SpaceUpdateRequestDto dto) {
        log.info("Updating space ID: {} by owner: {}", id, owner.getId());

        // 이미지 확인 로그
        log.info("Received image: {}", dto.getImage() != null ? dto.getImage().getOriginalFilename() : "null");
        if (dto.getImage() != null) {
            log.info("Image empty check: {}", dto.getImage().isEmpty());
        }

        Space space = spaceRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("공간이 없거나 수정 권한이 없습니다."));

        // 이미지가 새로 올라온 경우 교체
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            log.info("기존 이미지 URL: {}", space.getCoverImageUrl());

            String imageUrl = fileStorageService.save(dto.getImage());
            log.info("새로 저장된 이미지 URL: {}", imageUrl);

            space.updateCoverImage(imageUrl);
            log.info("엔티티 업데이트 후 이미지 URL: {}", space.getCoverImageUrl());
            log.info("Image updated for space ID: {}", id);
        } else {
            log.info("No image to update for space ID: {}", id);  // 이미지 업데이트 로그
        }

        // 나머지 정보 업데이트
        space.updateSpaceInfo(
                dto.getTitle(),
                dto.getDescription(),
                dto.getAddress(),
                dto.getAreaSize(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getRentalFee(),
                dto.getContactPhone()
        );

        log.info("Space updated successfully: {}", id);
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

    //내가 등록한 공간 단건 조회
    @Transactional(readOnly = true)
    public SpaceResponseDto getMine(User owner, Long id) {
        log.debug("Fetching space ID: {} for owner: {}", id, owner.getId());

        Space space = spaceRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("공간이 없거나 접근 권한이 없습니다."));

        return SpaceResponseDto.from(space);
    }
}
