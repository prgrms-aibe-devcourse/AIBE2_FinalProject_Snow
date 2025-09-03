package com.example.popin.domain.mission.entity;

import com.example.popin.global.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "mission")
@Getter
@Setter
public class Mission extends BaseEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @Type(type = "org.hibernate.type.UUIDBinaryType")
    private UUID id;

    private String title;
    private String description;

    // 정답(단순 비교용)
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_set_id")
    private MissionSet missionSet;
}
