package com.snow.popin.domain.notification.controller;

import com.snow.popin.domain.notification.dto.NotificationResponse;
import com.snow.popin.domain.notification.entity.Notification;
import com.snow.popin.domain.notification.entity.NotificationType;
import com.snow.popin.domain.notification.service.NotificationService;
import com.snow.popin.global.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final UserUtil userUtil;

    /** 내 알림 목록 조회 */
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        Long userId = userUtil.getCurrentUserId();

        List<Notification> list = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(
                list.stream()
                        .map(NotificationResponse::from)
                        .collect(Collectors.toList())
        );
    }
    /** 알림 읽음 처리 */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /** 예약 확정 시 알림 보내기 */
    @PostMapping("/api/notifications/reservation")
    public ResponseEntity<Void> sendReservationNotification(@RequestParam Long userId) {
        Notification n = notificationService.createNotification(
                userId,
                "예약 확정",
                "예약이 확정되었습니다!",
                NotificationType.RESERVATION,
                "/users/user-popup-reservation" // 알림 클릭 시 이동할 경로

        );

        /** 특정 유저에게 WebSocket push */
        messagingTemplate.convertAndSendToUser(
                userId.toString(), // userDestinationPrefix (/user) + userId
                "/queue/notifications",
                NotificationResponse.from(n)
        );

        return ResponseEntity.ok().build();
    }
}
