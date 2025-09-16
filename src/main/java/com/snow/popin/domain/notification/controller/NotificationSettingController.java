package com.snow.popin.domain.notification.controller;

import com.snow.popin.domain.notification.dto.NotificationSettingRequestDto;
import com.snow.popin.domain.notification.dto.NotificationSettingResponseDto;
import com.snow.popin.domain.notification.service.NotificationSettingService;
import com.snow.popin.global.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService settingService;
    private final UserUtil userUtil;

    /** 내 알림 설정 조회 */
    @GetMapping("/me")
    public ResponseEntity<NotificationSettingResponseDto> getMySettings() {
        Long userId = userUtil.getCurrentUserId();
        return ResponseEntity.ok(settingService.getSettings(userId));
    }

    /** 특정 타입 알림 설정 변경 */
    @PostMapping("/update")
    public ResponseEntity<Void> updateSetting(@RequestBody NotificationSettingRequestDto dto) {
        Long userId = userUtil.getCurrentUserId();
        settingService.updateSetting(userId, dto.getType(), dto.isEnabled());
        return ResponseEntity.ok().build();
    }

}
