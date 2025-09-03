package com.example.popin.domain.reward.entity;

import lombok.*;
import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "reward_option",
        indexes = {@Index(name="idx_option_mission_set", columnList = "mission_set_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RewardOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mission_set_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID missionSetId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private int issued;

    @Version
    private long version;

    public int getRemaining() { return Math.max(0, total - issued); }

    public void consumeOne() {
        if (getRemaining() <= 0) throw new IllegalStateException("OUT_OF_STOCK");
        issued++;
    }
}
