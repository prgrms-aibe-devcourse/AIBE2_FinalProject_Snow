package com.snow.popin.domain.notification.entity;

import com.snow.popin.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 설정 대상 유저
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 전체 알림 ON/OFF
    @Column(nullable = false)
    private boolean enabled = true;

    // 예약 관련 알림
    @Column(nullable = false)
    private boolean reservationEnabled = true;

    // 문의 관련 알림
    @Column(nullable = false)
    private boolean inquiryEnabled = true;

    // 시스템 알림
    @Column(nullable = false)
    private boolean systemEnabled = true;

    @Builder
    public NotificationSetting(User user,
                               boolean enabled,
                               boolean reservationEnabled,
                               boolean inquiryEnabled,
                               boolean systemEnabled) {
        this.user = user;
        this.enabled = enabled;
        this.reservationEnabled = reservationEnabled;
        this.inquiryEnabled = inquiryEnabled;
        this.systemEnabled = systemEnabled;
    }

    public void update(boolean enabled,
                       boolean reservationEnabled,
                       boolean inquiryEnabled,
                       boolean systemEnabled) {
        this.enabled = enabled;
        this.reservationEnabled = reservationEnabled;
        this.inquiryEnabled = inquiryEnabled;
        this.systemEnabled = systemEnabled;
    }
}
