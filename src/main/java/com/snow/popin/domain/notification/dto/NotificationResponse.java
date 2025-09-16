package com.snow.popin.domain.notification.dto;

import com.snow.popin.domain.notification.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private String message;
    private String type;
    private boolean read;
    private String title;
    private String link;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType().name())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .title(n.getTitle())
                .link(n.getLink())
                .build();
    }
}
