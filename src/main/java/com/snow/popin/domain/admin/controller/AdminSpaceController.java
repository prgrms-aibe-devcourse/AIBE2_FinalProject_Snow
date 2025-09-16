package com.snow.popin.domain.admin.controller;

import com.snow.popin.domain.admin.service.AdminSpaceService;
import com.snow.popin.domain.space.dto.SpaceListResponseDto;
import com.snow.popin.domain.space.dto.SpaceResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 관리자용 장소 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/spaces")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminSpaceController {

    private final AdminSpaceService adminSpaceService;

    /**
     * 장소 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSpaceStats(){
        Map<String, Object> stats = adminSpaceService.getSpaceStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 장소 목록 조회 (관리자용)
     */
    @GetMapping
    public ResponseEntity<Page<SpaceListResponseDto>> getSpace(
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean isPublic,
            @PageableDefault(size = 20, sort = "createdAt")Pageable pageable ){
        log.info("관리자 장소 대여 목록 조회 요청 - owner: {}, title: {}, isPublic: {}, 페이지: {}",
                owner, title, isPublic, pageable.getPageNumber());

        Page<SpaceListResponseDto> spaces = adminSpaceService.getSpaces(owner, title, isPublic, pageable);
        return ResponseEntity.ok(spaces);
    }

    /**
     * 장소 상세 조회 (관리자용)
     */
    @GetMapping("/{spaceId}")
    public ResponseEntity<SpaceResponseDto> getSpaceDetail(@PathVariable Long spaceId){
        log.info("관리자 장소 대역 상세 조회 ID: {}", spaceId);

        SpaceResponseDto space = adminSpaceService.getSpaceDetail(spaceId);
        return ResponseEntity.ok(space);
    }

    /**
     * 장소 비활성화 (관리자용)
     */
}
