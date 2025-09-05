package com.example.popin.domain.mission.service;

import com.example.popin.domain.mission.dto.SubmitAnswerResponseDto;
import com.example.popin.domain.mission.entity.Mission;
import com.example.popin.domain.mission.entity.UserMission;
import com.example.popin.domain.mission.entity.UserMissionStatus;
import com.example.popin.domain.mission.repository.MissionRepository;
import com.example.popin.domain.mission.repository.UserMissionRepository;
import com.example.popin.domain.user.entity.User;
import com.example.popin.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserMissionService {

    private final UserMissionRepository userMissionRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;

    public UserMissionService(UserMissionRepository userMissionRepository,
                              MissionRepository missionRepository,
                              UserRepository userRepository) {
        this.userMissionRepository = userMissionRepository;
        this.missionRepository = missionRepository;
        this.userRepository = userRepository;
    }

    /** 유저 미션 상태 생성 */
    @Transactional
    public UserMission create(Long userId, UUID missionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("미션 없음: " + missionId));

        return userMissionRepository.save(new UserMission(user, mission));
    }

    /** 유저 미션 단건 조회 */
    public Optional<UserMission> findById(Long id) {
        return userMissionRepository.findById(id);
    }

    /** 정답 제출 */
    @Transactional
    public SubmitAnswerResponseDto submitAnswer(UUID missionId, Long userId, String answer) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new NoSuchElementException("mission not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        UserMission um = userMissionRepository.findByUser_IdAndMission_Id(userId, missionId)
                .orElseGet(() -> userMissionRepository.save(new UserMission(user, mission)));

        boolean pass;
        if (um.getStatus() == UserMissionStatus.COMPLETED) {
            pass = true;
        } else {
            pass = isCorrect(mission.getAnswer(), answer);
            if (pass) {
                um.markCompleted();
            } else {
                um.markFail();
            }
            userMissionRepository.save(um);
        }

        long successCnt = userMissionRepository
                .countByUser_IdAndMission_MissionSet_IdAndStatus(
                        userId, mission.getMissionSet().getId(), UserMissionStatus.COMPLETED);

        boolean cleared = successCnt >= (mission.getMissionSet().getRequiredCount() == null ? 0
                : mission.getMissionSet().getRequiredCount());

        return SubmitAnswerResponseDto.builder()
                .pass(pass)
                .status(um.getStatus())
                .missionSetId(mission.getMissionSet().getId())
                .successCount(successCnt)
                .requiredCount(mission.getMissionSet().getRequiredCount())
                .cleared(cleared)
                .build();

    }

    private boolean isCorrect(String expected, String provided) {
        if (expected == null || provided == null) return false;
        return normalize(expected).equals(normalize(provided));
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
