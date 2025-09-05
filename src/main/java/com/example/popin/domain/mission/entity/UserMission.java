package com.example.popin.domain.mission.entity;

import com.example.popin.domain.user.entity.User;
import com.example.popin.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_mission",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_mission_user_mission",
                columnNames = {"user_id", "mission_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Mission FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserMissionStatus status = UserMissionStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // 생성자
    public UserMission(User user, Mission mission) {
        this.user = user;
        this.mission = mission;
        this.status = UserMissionStatus.PENDING;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public void setStatus(UserMissionStatus status) {
        this.status = status;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    // 비즈니스 메서드
    public void markCompleted() {
        this.status = UserMissionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markFail() {
        this.status = UserMissionStatus.FAIL;
        this.completedAt = LocalDateTime.now();
    }

    public void markPending() {
        this.status = UserMissionStatus.PENDING;
        this.completedAt = null;
    }

    public boolean isCompleted() {
        return this.status == UserMissionStatus.COMPLETED;
    }
}
