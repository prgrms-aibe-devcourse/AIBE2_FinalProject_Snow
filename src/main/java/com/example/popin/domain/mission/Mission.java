package com.example.popin.domain.mission;


import com.example.popin.domain.missionset.MissionSet;
import com.example.popin.domain.usermission.UserMission;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mission")
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String answer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "mission_set_id", nullable = false)
    private MissionSet missionSet;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL)
    private List<UserMission> userMissions = new ArrayList<>();
}
