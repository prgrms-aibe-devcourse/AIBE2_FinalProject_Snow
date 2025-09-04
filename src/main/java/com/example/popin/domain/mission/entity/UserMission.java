package com.example.popin.domain.mission.entity;

import com.example.popin.domain.user.entity.User;
import com.example.popin.global.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@Setter
@ToString(exclude = {"user", "mission"})
public class UserMission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    // Mission FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserMissionStatus status = UserMissionStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
