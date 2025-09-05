package com.snow.popin.domain.mission.entity;

import com.snow.popin.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionSet extends BaseEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @Type(type = "org.hibernate.type.UUIDBinaryType")
    private UUID id;

    @Column(name = "popup_id", nullable = false)
    private Long popupId;

    @Column(name = "required_count")
    private Integer requiredCount;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // 기본값 예시

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "missionSet", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Mission> missions = new ArrayList<>();

    @Column(name = "reward_pin", length = 80)
    private String rewardPin;

    // 생성자
    @Builder
    public MissionSet(Long popupId, Integer requiredCount, String status, String rewardPin) {
        this.popupId = popupId;
        this.requiredCount = requiredCount;
        this.status = status;
        this.rewardPin = rewardPin;
    }

    // 비즈니스 메서드
    public void addMission(Mission mission) {
        this.missions.add(mission);
        mission.setMissionSet(this);
    }

    public void complete() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }
}
