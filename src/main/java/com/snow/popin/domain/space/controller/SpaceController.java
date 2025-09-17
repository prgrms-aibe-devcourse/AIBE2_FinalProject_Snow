package com.snow.popin.domain.space.controller;

import com.snow.popin.domain.space.dto.SpaceCreateRequestDto;
import com.snow.popin.domain.space.dto.SpaceListResponseDto;
import com.snow.popin.domain.space.dto.SpaceResponseDto;
import com.snow.popin.domain.space.dto.SpaceUpdateRequestDto;
import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.space.repository.SpaceRepository;
import com.snow.popin.domain.space.service.SpaceService;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spaces")
@Slf4j
public class SpaceController {

    private final SpaceService spaceService;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;

    /**
     * 모든 공간 목록 조회
     */
    @GetMapping
    public List<SpaceListResponseDto> listAllSpaces() {
        User me = getCurrentUser();
        log.debug("Listing all spaces");
        return spaceService.listAll(me);
    }

    /**
     * 공간 검색
     * @param keyword 제목, 설명에서 검색할 키워드 (선택)
     * @param location 주소에서 검색할 위치 (선택)
     * @param minArea 최소 면적 (선택)
     * @param maxArea 최대 면적 (선택)
     * @return 검색 조건에 맞는 공간 목록
     */
    @GetMapping("/search")
    public List<SpaceListResponseDto> searchSpaces(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minArea,
            @RequestParam(required = false) Integer maxArea
    ) {
        User me = getCurrentUser();
        log.debug("Searching spaces - keyword: {}, location: {}, minArea: {}, maxArea: {}",
                keyword, location, minArea, maxArea);
        return spaceService.searchSpaces(me, keyword, location, minArea, maxArea);
    }

    /**
     * 내 등록글 조회
     */
    @GetMapping("/mine")
    public List<SpaceListResponseDto> listMySpaces() {
        User me = getCurrentUser();
        log.debug("Listing spaces for user: {}", me.getEmail());
        return spaceService.listMine(me);
    }

    /**
     * 공간 상세 조회
     */
    @GetMapping("/{id}")
    public SpaceResponseDto getDetail(@PathVariable Long id) {
        User me = getCurrentUserOrNull();
        return spaceService.getDetail(me, id);
    }

    /**
     * 공간 등록
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @ModelAttribute SpaceCreateRequestDto dto,
                                    BindingResult br) {
        if (br.hasErrors()) {
            return badRequest(br);
        }
        User me = getCurrentUser();
        Long id = spaceService.create(me, dto);
        log.info("Space created by {} with ID {}", me.getEmail(), id);
        return ResponseEntity.ok(Map.of("id", id));
    }

    /**
     * 공간 게시글 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @ModelAttribute SpaceUpdateRequestDto dto,
                                    BindingResult br) {
        if (br.hasErrors()) {
            return badRequest(br);
        }
        User me = getCurrentUser();
        spaceService.update(me, id, dto);
        log.info("Space updated by {} with ID {}", me.getEmail(), id);
        return ResponseEntity.ok().build();
    }

    /**
     * 공간 게시글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        User me = getCurrentUser();
        spaceService.delete(me, id);
        log.info("Space deleted by {} with ID {}", me.getEmail(), id);
        return ResponseEntity.ok().build();
    }

    /**
     * 문의
     */
    @PostMapping("/{id}/inquiries")
    public ResponseEntity<?> createInquiry(@PathVariable Long id) {
        return ResponseEntity.accepted().build(); // 202 Accepted
    }

    /**
     * 신고
     */
    @PostMapping("/{id}/reports")
    public ResponseEntity<?> report(@PathVariable Long id) {
        User me = getCurrentUser();
        spaceService.hideSpace(me, id); // 신고 시 숨김 처리
        return ResponseEntity.ok(Map.of("message", "신고가 접수되어 해당 공간이 숨겨졌습니다."));
    }

    /**
     * 공간 숨김 처리 (신고 시)
     */
    @Transactional
    public void hideSpace(User reporter, Long spaceId) {
        log.info("Hiding space ID: {} reported by user: {}", spaceId, reporter.getId());

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공간이 존재하지 않습니다."));

        // 자신의 공간은 신고할 수 없음
        if (space.isOwner(reporter)) {
            throw new IllegalArgumentException("자신의 공간은 신고할 수 없습니다.");
        }

        space.hide();
        log.info("Space ID {} hidden successfully", spaceId);
    }

    /**
     * 유효성 검증 실패 시 에러 응답 생성
     */
    private ResponseEntity<Map<String, Object>> badRequest(BindingResult br) {
        Map<String, String> errors = new HashMap<>();
        br.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    /**
     * 현재 로그인한 User 조회 (userutil로 삭제 할것 )
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."));
    }

    /**
     * 현재 사용자 조회 (비로그인 허용)
     */
    private User getCurrentUserOrNull() {
        try {
            return getCurrentUser();
        } catch (ResponseStatusException e) {
            return null; // unauthenticated → treat as anonymous
        }
    }
}