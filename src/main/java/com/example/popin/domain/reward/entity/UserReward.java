package com.example.popin.domain.reward.entity;

import com.example.popin.global.common.BaseEntity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_reward",
        uniqueConstraints = {
                // 같은 미션셋에 대해 사용자당 1회만 발급
                @UniqueConstraint(name = "uk_user_mission_set_once", columnNames = {"user_id","mission_set_id"})
        },
        indexes = {
                // 조회 최적화(유저의 해당 미션셋 리워드, 상태별 조회)
                @Index(name = "idx_user_set_status", columnList = "user_id, mission_set_id, status")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserReward extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="mission_set_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID missionSetId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="reward_option_id", nullable = false)
    private RewardOption option; // 어떤 옵션으로 발급됐는지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRewardStatus status; // ISSUED / REDEEMED / CANCELED

    @Column(name = "redeemed_at")
    private LocalDateTime redeemedAt; // 수령 완료 시각

    /** 발급 시각은 BaseEntity의 created_at 사용 */
    @Transient
    public LocalDateTime getIssuedAt() { return super.getCreatedAt(); }

    /** 상태 전환 헬퍼(선택) */
    public void markRedeemed() {
        this.status = UserRewardStatus.REDEEMED;
        this.redeemedAt = LocalDateTime.now();
    }
}
