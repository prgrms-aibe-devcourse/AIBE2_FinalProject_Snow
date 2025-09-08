package com.snow.popin.domain.mission.service;

import com.snow.popin.domain.mission.dto.SubmitAnswerResponseDto;
import com.snow.popin.domain.mission.entity.Mission;
import com.snow.popin.domain.mission.entity.UserMission;
import com.snow.popin.domain.mission.entity.UserMissionStatus;
import com.snow.popin.domain.mission.repository.MissionRepository;
import com.snow.popin.domain.mission.repository.UserMissionRepository;
import com.snow.popin.domain.user.UserRepository;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.exception.MissionException;
import com.snow.popin.global.exception.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 유저 미션 상태 생성
    @Transactional
    public UserMission create(Long userId, UUID missionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNotFound(userId));
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(MissionException.MissionNotFound::new);

        return userMissionRepository.save(new UserMission(user, mission));
    }

    //유저 미션 단건 조회
    public Optional<UserMission> findById(Long id) {
        return userMissionRepository.findById(id);
    }

    // 정답 제출
    @Transactional
    public SubmitAnswerResponseDto submitAnswer(UUID missionId, Long userId, String answer) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(MissionException.MissionNotFound::new);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNotFound(userId));

        UserMission userMission = userMissionRepository.findByUser_IdAndMission_Id(userId, missionId)
                .orElseGet(() -> userMissionRepository.save(new UserMission(user, mission)));

        boolean pass;
        if (userMission.getStatus() == UserMissionStatus.COMPLETED) {
            pass = true; // 이미 완료된 경우 pass
        } else {
            pass = isCorrect(mission.getAnswer(), answer);
            if (pass) {
                userMission.markCompleted();
            } else {
                userMission.markFail();
                throw new MissionException.InvalidAnswer();
            }
            userMissionRepository.save(userMission);
        }

        long successCnt = userMissionRepository
                .countByUser_IdAndMission_MissionSet_IdAndStatus(
                        userId, mission.getMissionSet().getId(), UserMissionStatus.COMPLETED);

        boolean cleared = successCnt >=
                Optional.ofNullable(mission.getMissionSet().getRequiredCount()).orElse(0);

        return SubmitAnswerResponseDto.builder()
                .pass(pass)
                .status(userMission.getStatus())
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
