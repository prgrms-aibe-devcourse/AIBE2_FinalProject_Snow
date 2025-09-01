package com.example.popin.domain.space.controller;

import com.example.popin.domain.space.dto.*;
import com.example.popin.domain.space.service.SpaceService;
import com.example.popin.domain.user.User;
import com.example.popin.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final UserRepository userRepository;

    // 모든 공간 목록 조회 (새로 추가)
    @GetMapping // 새로운 엔드포인트
    public List<SpaceListResponseDto> listAllSpaces() {
        log.debug("Listing all spaces");
        return spaceService.listAll();
    }

    //내 등록글 조회
    @GetMapping("/mine")
    public List<SpaceListResponseDto> listMySpaces() {
        User me = getCurrentUser();
        log.debug("Listing spaces for user: {}", me.getEmail());
        return spaceService.listMine(me);
    }

    //공간 단건 조회
    @GetMapping("/{id}")
    public SpaceResponseDto getOne(@PathVariable Long id) {
        User me = getCurrentUser();
        log.debug("Fetching space ID: {} for user: {}", id, me.getEmail());
        return spaceService.getMine(me, id);
    }

    //공간 등록
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



    //공간 게시글 수정
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

    //공간 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        User me = getCurrentUser();
        spaceService.delete(me, id);
        log.info("Space deleted by {} with ID {}", me.getEmail(), id);
        return ResponseEntity.ok().build();
    }

    //공통: 유효성 검증 실패 시 에러 응답 생성
    private ResponseEntity<Map<String, Object>> badRequest(BindingResult br) {
        Map<String, String> errors = new HashMap<>();
        br.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    //현재 로그인한 User 조회
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null) ? auth.getName() : null;

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return user;
    }

}
