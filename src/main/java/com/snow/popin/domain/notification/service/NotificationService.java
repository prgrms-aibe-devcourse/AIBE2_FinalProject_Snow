package com.snow.popin.domain.notification.service;

import com.snow.popin.domain.notification.dto.NotificationResponse;
import com.snow.popin.domain.notification.entity.Notification;
import com.snow.popin.domain.notification.entity.NotificationSetting;
import com.snow.popin.domain.notification.entity.NotificationType;
import com.snow.popin.domain.notification.repository.NotificationRepository;
import com.snow.popin.domain.notification.repository.NotificationSettingRepository;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.domain.user.repository.UserRepository;
import com.snow.popin.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final SimpMessagingTemplate messagingTemplate; // 웹소켓 메시지 전송용
    private final UserRepository userRepository;

    @Transactional
    public Notification sendNotification(User user, String message, NotificationType type, String title, String link) {
        Notification notification = new Notification(user, message, type, title, link);
        Notification saved = notificationRepository.save(notification);

        // WebSocket 구독자에게 푸시
        messagingTemplate.convertAndSendToUser(
                user.getId().toString(), // userId 기준 구독
                "/queue/notifications",
                saved
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. id=" + userId));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }


    @Transactional
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
        n.markAsRead();
    }

    @Transactional
    public Notification createNotification(Long userId, String title, String message, NotificationType type, String link) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 유저의 알림 설정 조회
        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("알림 설정이 없습니다."));

        // 전체 알림 off → 바로 종료
        if (!setting.isEnabled()) {
            return null;
        }

        // 타입별 알림 off → 종료
        switch (type) {
            case RESERVATION:
                if (!setting.isReservationEnabled()) return null;
                break;
            case SYSTEM:
                if (!setting.isSystemEnabled()) return null;
                break;
            case EVENT:
                if (!setting.isInquiryEnabled()) return null;
                break;
        }

        // 알림 생성
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .link(link)
                .build();

        Notification saved = notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                user.getId().toString(),
                "/queue/notifications",
                NotificationResponse.from(saved)
        );

        return saved;
    }


}
