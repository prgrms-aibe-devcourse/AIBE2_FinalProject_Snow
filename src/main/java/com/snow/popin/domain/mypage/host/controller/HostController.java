package com.snow.popin.domain.mypage.host.controller;

import com.snow.popin.domain.mypage.host.dto.HostProfileResponseDto;
import com.snow.popin.domain.mypage.host.dto.PopupRegisterRequestDto;
import com.snow.popin.domain.mypage.host.dto.PopupRegisterResponseDto;
import com.snow.popin.domain.mypage.host.service.HostService;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosts")
@RequiredArgsConstructor
public class HostController {

    private final HostService hostService;
    private final UserUtil userUtil;

    //  팝업 등록
    @PostMapping("/popups")
    public ResponseEntity<?> createPopup(@RequestBody PopupRegisterRequestDto dto) {
        User currentUser = userUtil.getCurrentUser();
        Long id = hostService.createPopup(currentUser, dto);
        return ResponseEntity.ok(Map.of("id", id, "message", "팝업 등록 완료"));
    }

    //  내 팝업 조회
    @GetMapping("/popups")
    public ResponseEntity<List<PopupRegisterResponseDto>> getMyPopups() {
        User currentUser = userUtil.getCurrentUser();
        return ResponseEntity.ok(hostService.getMyPopups(currentUser));
    }

    // 팝업 수정
    @PutMapping("/popups/{id}")
    public ResponseEntity<?> updatePopup(@PathVariable Long id,
                                         @RequestBody PopupRegisterRequestDto dto) {
        User user = userUtil.getCurrentUser();
        hostService.updatePopup(user, id, dto);
        return ResponseEntity.ok(Map.of("message", "팝업 수정 완료"));
    }

    // 팝업 삭제
    @DeleteMapping("/popups/{id}")
    public ResponseEntity<?> deletePopup(@PathVariable Long id) {
        User user = userUtil.getCurrentUser();
        hostService.deletePopup(user, id);
        return ResponseEntity.ok(Map.of("message", "팝업 삭제 완료"));
    }

    //내 정보
    @GetMapping("/me")
    public ResponseEntity<HostProfileResponseDto> getMyHostProfile() {
        User user = userUtil.getCurrentUser();
        return ResponseEntity.ok(hostService.getMyHostProfile(user));
    }

}
