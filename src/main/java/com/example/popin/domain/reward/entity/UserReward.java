package com.example.popin.domain.reward.entity;

import lombok.*;
import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_reward",
        uniqueConstraints = {
                // 같은 미션셋에 대해 사용자당 1회만 발급
                @UniqueConstraint(name="uk_user_mission_set_once", columnNames = {"user_id","mission_set_id"})
        },
        indexes = {
                @Index(name="idx_code_unique", columnList = "code", unique = true)
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserReward {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="mission_set_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID missionSetId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="reward_option_id")
    private RewardOption option;     // 어떤 옵션으로 발급됐는지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRewardStatus status; // ISSUED/REDEEMED/CANCELED

    @Column(nullable = false, unique = true, length = 32)
    private String code;             // 사용자 제시 코드 (8~12자 권장)

    private Instant issuedAt;
    private Instant redeemedAt;
}
