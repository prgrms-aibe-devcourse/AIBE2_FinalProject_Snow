package com.example.popin.domain.missionset;

import com.example.popin.domain.mission.Mission;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "mission_set")
public class MissionSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "popup_id", nullable = false)
    private Long popupId;

    private Integer requiredCount;
    private String status;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "missionSet", cascade = CascadeType.ALL)
    private List<Mission> missions = new ArrayList<>();
}
