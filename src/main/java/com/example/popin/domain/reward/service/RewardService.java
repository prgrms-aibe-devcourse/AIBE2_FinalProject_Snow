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

    private final RewardOptionRepository optionRepository;
    private final UserRewardRepository rewardRepository;
    private final MissionSetRepository missionSetRepository;
    private final UserMissionRepository userMissionRepository;

    @Transactional(readOnly = true)
    public List<RewardOption> listOptions(UUID missionSetId) {
        return optionRepository.findByMissionSetId(missionSetId);
    }

    // 발급: 유저당 1회 / 미션 조건 충족 / 옵션 재고 차감
    @Transactional
    public UserReward claim(UUID missionSetId, Long optionId, Long userId) {
        // 이미 발급된 게 있으면 그대로 반환(idempotent)
        var existing = rewardRepository.findByUserIdAndMissionSetId(userId, missionSetId);
        if (existing.isPresent()) return existing.get();

        // 미션 조건 확인
        MissionSet set = missionSetRepository.findById(missionSetId)
                .orElseThrow(() -> new IllegalArgumentException("NO_MISSION_SET"));
        int required = Optional.ofNullable(set.getRequiredCount()).orElse(0);
        long success = userMissionRepository.countByUser_IdAndMission_MissionSet_IdAndStatus(
                userId, missionSetId, UserMissionStatus.COMPLETED);
        if (success < required) throw new IllegalStateException("NOT_CLEARED");

        // 옵션 잠금 + 재고 차감
        RewardOption opt = optionRepository.lockById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("INVALID_OPTION"));
        if (!opt.getMissionSetId().equals(missionSetId))
            throw new IllegalArgumentException("OPTION_NOT_IN_SET");
        opt.consumeOne(); // 재고 없으면 OUT_OF_STOCK

        // 지급 레코드 생성
        UserReward rw = UserReward.builder()
                .userId(userId)
                .missionSetId(missionSetId)
                .option(opt)
                .status(UserRewardStatus.ISSUED)
                .build();
        return rewardRepository.save(rw);
    }

    @Transactional
    public UserReward redeem(UUID missionSetId, Long userId, String staffPinPlain) {
        UserReward rw = rewardRepository.findByUserIdAndMissionSetIdAndStatus(
                userId, missionSetId, UserRewardStatus.ISSUED
        ).orElseThrow(() -> new IllegalStateException("NOT_ISSUED"));

        MissionSet set = missionSetRepository.findById(missionSetId)
                .orElseThrow(() -> new IllegalStateException("NO_MISSION_SET"));

        String stored = set.getRewardPin(); // 평문 저장 사용
        if (stored == null || stored.isBlank())
            throw new IllegalStateException("NO_STAFF_PIN");

        if (!Objects.equals(stored, staffPinPlain)) {
            throw new IllegalArgumentException("INVALID_STAFF_PIN");
        }

        rw.markRedeemed();

        return rw;
    }

    @Transactional(readOnly = true)
    public Optional<UserReward> findUserReward(Long userId, UUID missionSetId) {
        return rewardRepository.findByUserIdAndMissionSetId(userId, missionSetId);
    }

}
