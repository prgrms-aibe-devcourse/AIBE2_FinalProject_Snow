package com.example.popin.domain.reward.entity;

import com.example.popin.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(
        name = "reward_option",
        indexes = {
                @Index(name = "idx_option_mission_set", columnList = "mission_set_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mission_set_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID missionSetId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int total;

    // 발급된 개수
    @Column(nullable = false)
    private int issued;

    @Version
    private long version;

    // 생성자
    public RewardOption(UUID missionSetId, String name, int total) {
        this.missionSetId = missionSetId;
        this.name = name;
        this.total = total;
        this.issued = 0;
    }

    // 남은 재고
    public int getRemaining() {
        return Math.max(0, total - issued);
    }

    // 재고 차감 (초과 방지)
    public void consumeOne() {
        if (getRemaining() <= 0) {
            throw new IllegalStateException("OUT_OF_STOCK");
        }
        issued++;
    }
}
