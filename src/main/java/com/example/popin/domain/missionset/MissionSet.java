package com.example.popin.domain.missionset;

import com.example.popin.domain.mission.Mission;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mission_set")
@Getter
@Setter
public class MissionSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "popup_id", nullable = false)
    private Long popupId;

    @Column(name = "required_count")
    private Integer requiredCount;

    private String status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "missionSet", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Mission> missions = new ArrayList<>();
}
