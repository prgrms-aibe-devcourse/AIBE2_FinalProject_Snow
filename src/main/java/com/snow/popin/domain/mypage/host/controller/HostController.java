package com.snow.popin.domain.mypage.host.controller;

import com.snow.popin.domain.mypage.host.dto.HostProfileResponseDto;
import com.snow.popin.domain.mypage.host.dto.PopupRegisterRequestDto;
import com.snow.popin.domain.mypage.host.dto.PopupRegisterResponseDto;
import com.snow.popin.domain.mypage.host.service.HostService;
import com.snow.popin.domain.space.service.FileStorageService;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 호스트 관련 REST 컨트롤러
 *
 * - 팝업 등록/조회/수정/삭제
 * - 호스트 프로필 조회
 */
@RestController
@RequestMapping("/api/hosts")
@RequiredArgsConstructor
public class HostController {

    private final HostService hostService;
    private final UserUtil userUtil;
    private final FileStorageService fileStorageService;

    /**
     * 팝업 등록
     *
     * @param dto 팝업 등록 요청 DTO
     * @return 팝업 ID 및 성공 메시지
     */
    @PostMapping("/popups")
    public ResponseEntity<?> createPopup(@RequestBody PopupRegisterRequestDto dto) {
        User currentUser = userUtil.getCurrentUser();
        Long id = hostService.createPopup(currentUser, dto);
        return ResponseEntity.ok(Map.of("id", id, "message", "팝업 등록 완료"));
    }
    /**
     * 내가 등록한 팝업 목록 조회
     *
     * @return 팝업 응답 DTO 리스트
     */
    @GetMapping("/popups")
    public ResponseEntity<Page<PopupRegisterResponseDto>> getMyPopups(Pageable pageable) {
        User currentUser = userUtil.getCurrentUser();
        return ResponseEntity.ok(hostService.getMyPopups(currentUser, pageable));
    }
    /**
     * 내가 등록한 팝업 상세 조회
     *
     * @param id 팝업 ID
     * @return 팝업 응답 DTO
     */
    @GetMapping("/popups/{id}")
    public ResponseEntity<PopupRegisterResponseDto> getMyPopupDetail(@PathVariable Long id) {
        User currentUser = userUtil.getCurrentUser();
        return ResponseEntity.ok(hostService.getMyPopupDetail(currentUser, id));
    }
    /**
     * 팝업 수정
     *
     * @param id 팝업 ID
     * @param dto 팝업 수정 요청 DTO
     * @return 성공 메시지
     */
    @PutMapping("/popups/{id}")
    public ResponseEntity<?> updatePopup(@PathVariable Long id,
                                         @RequestBody PopupRegisterRequestDto dto) {
        User user = userUtil.getCurrentUser();
        hostService.updatePopup(user, id, dto);
        return ResponseEntity.ok(Map.of("message", "팝업 수정 완료"));
    }
    /**
     * 팝업 삭제
     *
     * @param id 팝업 ID
     * @return 성공 메시지
     */
    @DeleteMapping("/popups/{id}")
    public ResponseEntity<?> deletePopup(@PathVariable Long id) {
        User user = userUtil.getCurrentUser();
        hostService.deletePopup(user, id);
        return ResponseEntity.ok(Map.of("message", "팝업 삭제 완료"));
    }
    /**
     * 내 호스트 프로필 조회
     *
     * @return 호스트 프로필 응답 DTO
     */
    @GetMapping("/me")
    public ResponseEntity<HostProfileResponseDto> getMyHostProfile() {
        User user = userUtil.getCurrentUser();
        return ResponseEntity.ok(hostService.getMyHostProfile(user));
    }
    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            String imageUrl = fileStorageService.save(file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
