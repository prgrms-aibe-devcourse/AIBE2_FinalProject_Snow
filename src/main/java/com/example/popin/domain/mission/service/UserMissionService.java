package com.example.popin.domain.mission.service;

import com.example.popin.domain.mission.entity.Mission;
import com.example.popin.domain.mission.entity.UserMission;
import com.example.popin.domain.mission.entity.UserMissionStatus;
import com.example.popin.domain.mission.repository.MissionRepository;
import com.example.popin.domain.user.User;
import com.example.popin.domain.user.UserRepository;
import com.example.popin.domain.mission.dto.SubmitAnswerResponseDto;
import com.example.popin.domain.mission.repository.UserMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public UserMission create(UserMission userMission) {
        return userMissionRepository.save(userMission);
    }

    public Optional<UserMission> findById(Long id) {
        return userMissionRepository.findById(id);
    }

    @Transactional
    public SubmitAnswerResponseDto submitAnswer(UUID missionId, Long userId, String answer) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new NoSuchElementException("mission not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        UserMission um = userMissionRepository.findByUser_IdAndMission_Id(userId, missionId)
                .orElseGet(() -> {
                    UserMission n = new UserMission();
                    n.setUser(user);
                    n.setMission(mission);
                    n.setStatus(UserMissionStatus.PENDING);
                    return userMissionRepository.save(n);
                });

        boolean pass;
        if (um.getStatus() == UserMissionStatus.SUCCESS) {
            pass = true;
        } else {
            pass = isCorrect(mission.getAnswer(), answer);
            if (pass) {
                um.setStatus(UserMissionStatus.SUCCESS);
                um.setCompletedAt(LocalDateTime.now());
            } else {
                um.setStatus(UserMissionStatus.FAIL);
            }
            userMissionRepository.save(um);
        }

        long successCnt = userMissionRepository
                .countByUser_IdAndMission_MissionSet_IdAndStatus(
                        userId, mission.getMissionSet().getId(), UserMissionStatus.SUCCESS);

        boolean cleared = successCnt >= (mission.getMissionSet().getRequiredCount() == null ? 0
                : mission.getMissionSet().getRequiredCount());

        SubmitAnswerResponseDto dto = new SubmitAnswerResponseDto();
        dto.setPass(pass);
        dto.setStatus(um.getStatus());
        dto.setMissionSetId(mission.getMissionSet().getId());
        dto.setSuccessCount(successCnt);
        dto.setRequiredCount(mission.getMissionSet().getRequiredCount());
        dto.setCleared(cleared);
        return dto;
    }

    private boolean isCorrect(String expected, String provided) {
        if (expected == null || provided == null) return false;
        String a = normalize(expected);
        String b = normalize(provided);
        return a.equals(b);
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
