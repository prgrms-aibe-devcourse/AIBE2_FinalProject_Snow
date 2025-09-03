package com.example.popin.domain.reward.service;

import com.example.popin.domain.mission.entity.MissionSet;
import com.example.popin.domain.mission.entity.UserMissionStatus;
import com.example.popin.domain.mission.repository.MissionSetRepository;
import com.example.popin.domain.mission.repository.UserMissionRepository;
import com.example.popin.domain.reward.entity.*;
import com.example.popin.domain.reward.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardOptionRepository optionRepo;
    private final UserRewardRepository rewardRepo;
    private final MissionSetRepository missionSetRepo;
    private final UserMissionRepository userMissionRepo;

    @Transactional(readOnly = true)
    public List<RewardOption> listOptions(UUID missionSetId) {
        return optionRepo.findByMissionSetId(missionSetId);
    }

    /** 발급: 유저당 1회 / 미션 조건 충족 / 옵션 재고 차감 */
    @Transactional
    public UserReward claim(UUID missionSetId, Long optionId, Long userId) {
        // 이미 발급된 게 있으면 그대로 반환(idempotent)
        var existing = rewardRepo.findByUserIdAndMissionSetId(userId, missionSetId);
        if (existing.isPresent()) return existing.get();

        // 미션 조건 확인
        MissionSet set = missionSetRepo.findById(missionSetId)
                .orElseThrow(() -> new IllegalArgumentException("NO_MISSION_SET"));
        int required = Optional.ofNullable(set.getRequiredCount()).orElse(0);
        long success = userMissionRepo.countByUser_IdAndMission_MissionSet_IdAndStatus(
                userId, missionSetId, UserMissionStatus.SUCCESS);
        if (success < required) throw new IllegalStateException("NOT_CLEARED");

        // 옵션 잠금 + 재고 차감
        RewardOption opt = optionRepo.lockById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("INVALID_OPTION"));
        if (!opt.getMissionSetId().equals(missionSetId))
            throw new IllegalArgumentException("OPTION_NOT_IN_SET");
        opt.consumeOne(); // 재고 없으면 OUT_OF_STOCK

        // 지급 레코드 생성 (createdAt은 BaseEntity/Auditing으로 자동)
        UserReward rw = UserReward.builder()
                .userId(userId)
                .missionSetId(missionSetId)
                .option(opt)
                .status(UserRewardStatus.ISSUED)
                .build();
        return rewardRepo.save(rw);
    }

    @Transactional
    public UserReward redeem(UUID missionSetId, Long userId, String staffPinPlain) {
        UserReward rw = rewardRepo.findByUserIdAndMissionSetIdAndStatus(
                userId, missionSetId, UserRewardStatus.ISSUED
        ).orElseThrow(() -> new IllegalStateException("NOT_ISSUED"));

        MissionSet set = missionSetRepo.findById(missionSetId)
                .orElseThrow(() -> new IllegalStateException("NO_MISSION_SET"));

        String stored = set.getRewardPin(); // 평문 저장 사용
        if (stored == null || stored.isBlank())
            throw new IllegalStateException("NO_STAFF_PIN");

        if (!Objects.equals(stored, staffPinPlain)) {
            throw new IllegalArgumentException("INVALID_STAFF_PIN");
        }

        rw.setStatus(UserRewardStatus.REDEEMED);
        rw.setRedeemedAt(LocalDateTime.now());
        return rw;
    }

    @Transactional(readOnly = true)
    public Optional<UserReward> findUserReward(Long userId, UUID missionSetId) {
        return rewardRepo.findByUserIdAndMissionSetId(userId, missionSetId);
    }

}
