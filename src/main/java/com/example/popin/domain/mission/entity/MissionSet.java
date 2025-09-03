package com.example.popin.domain.mission.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mission_set")
@Getter
@Setter
public class MissionSet {

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

    private String status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "missionSet", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Mission> missions = new ArrayList<>();

    @Column(name = "reward_pin", length = 80)
    private String rewardPin;
}
