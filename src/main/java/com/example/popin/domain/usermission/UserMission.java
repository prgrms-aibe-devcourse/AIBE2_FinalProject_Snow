package com.example.popin.domain.usermission;

import com.example.popin.domain.mission.Mission;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "user_mission")
public class UserMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // User 엔티티 있으면 FK 매핑 가능
    private String status;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;
}
