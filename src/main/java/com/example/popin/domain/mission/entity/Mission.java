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

    @Column(nullable = false, length = 100)
    private String title;
    @Column(length = 1000)
    private String description;

    // 정답(단순 비교용) — consider storing salted hash instead
    @Column(name = "answer", length = 255)
    private String answer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_set_id", nullable = false)
    private MissionSet missionSet;
}
