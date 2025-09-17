package com.snow.popin.domain.admin.service;

import com.snow.popin.domain.mission.dto.*;
import com.snow.popin.domain.mission.entity.Mission;
import com.snow.popin.domain.mission.entity.MissionSet;
import com.snow.popin.domain.mission.entity.MissionSetStatus;
import com.snow.popin.domain.mission.repository.MissionRepository;
import com.snow.popin.domain.mission.repository.MissionSetRepository;
import com.snow.popin.global.exception.MissionException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMissionService {

    private final MissionSetRepository missionSetRepository;
    private final MissionRepository missionRepository;

    // 목록 조회
    public Page<MissionSetAdminDto> getMissionSets(Pageable pageable, Long popupId, MissionSetStatus status) {
        Page<MissionSet> sets;
        if (popupId != null) {
            sets = missionSetRepository.findByPopupId(popupId, pageable);
        } else if (status != null) {
            sets = missionSetRepository.findByStatus(status, pageable);
        } else {
            sets = missionSetRepository.findAll(pageable);
        }
        return sets.map(MissionSetAdminDto::from);
    }

    // 상세 조회
    public MissionSetAdminDto getMissionSetDetail(UUID id) {
        MissionSet set = missionSetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MissionSet not found"));
        return MissionSetAdminDto.from(set);
    }

    // 생성
    public MissionSetAdminDto createMissionSet(MissionSetCreateRequestDto req) {
        MissionSet set = MissionSet.builder()
                .popupId(req.getPopupId())
                .requiredCount(req.getRequiredCount())
                .status(req.getStatus())
                .rewardPin(req.getRewardPin())
                .build();
        missionSetRepository.save(set);
        return MissionSetAdminDto.from(set);
    }

    // 삭제
    public void deleteMissionSet(UUID id) {
        missionSetRepository.deleteById(id);
    }

    // 미션 추가
    public MissionDto addMission(UUID setId, MissionCreateRequestDto req) {
        MissionSet set = missionSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("MissionSet not found"));
        Mission mission = Mission.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .answer(req.getAnswer())
                .missionSet(set)
                .build();
        missionRepository.save(mission);
        return MissionDto.from(mission);
    }

    // 미션 삭제
    public void deleteMission(UUID missionId) {
        missionRepository.deleteById(missionId);
    }

    @Transactional
    public MissionSetAdminDto updateMissionSet(UUID id, MissionSetUpdateRequestDto request) {
        MissionSet set = missionSetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MissionSet not found: " + id));

        if (request.getRequiredCount() != null) {
            set.setRequiredCount(request.getRequiredCount());
        }
        if (request.getStatus() != null) {
            set.setStatus(request.getStatus());
        }
        if (request.getRewardPin() != null) {
            set.setRewardPin(request.getRewardPin());
        }

        return MissionSetAdminDto.from(set);
    }

}
