package com.example.popin.domain.space.controller;

import com.example.popin.domain.space.dto.*;
import com.example.popin.domain.space.service.SpaceService;
import com.example.popin.domain.user.User;
import com.example.popin.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spaces")
@Slf4j
public class SpaceController {

    private final SpaceService spaceService;
    private final UserRepository userRepository;

    // 모든 공간 목록 조회
    @GetMapping
    public List<SpaceListResponseDto> listAllSpaces() {
        User me = getCurrentUser();
        log.debug("Listing all spaces");
        return spaceService.listAll(me);
    }

    //내 등록글 조회
    @GetMapping("/mine")
    public List<SpaceListResponseDto> listMySpaces() {
        User me = getCurrentUser();
        log.debug("Listing spaces for user: {}", me.getEmail());
        return spaceService.listMine(me);
    }

    //공간 상세 조회
    @GetMapping("/{id}")
    public SpaceResponseDto getDetail(@PathVariable Long id) {
        User me = getCurrentUserOrNull();
        return spaceService.getDetail(me, id);
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

    //문의
    @PostMapping("/{id}/inquiries")
    public ResponseEntity<?> createInquiry(@PathVariable Long id) {
        return ResponseEntity.accepted().build(); // 202 Accepted
    }

    //신고
    @PostMapping("/{id}/reports")
    public ResponseEntity<?> report(@PathVariable Long id) {
        return ResponseEntity.accepted().build();
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
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        String email = auth.getName();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"존재하지 않는 사용자입니다.");
        }
        return user;
    }
    //모든 사용자
    private User getCurrentUserOrNull() {
        try {
            return getCurrentUser();
        } catch (ResponseStatusException e) {
            return null; //unauthenticated -> treat as anonymous
        }
    }

}
