package com.snow.popin.domain.mission.service;

import com.snow.popin.domain.mission.dto.MissionSetViewDto;
import com.snow.popin.domain.mission.dto.MissionSummaryDto;
import com.snow.popin.domain.mission.entity.MissionSet;
import com.snow.popin.domain.mission.entity.UserMission;
import com.snow.popin.domain.mission.entity.UserMissionStatus;
import com.snow.popin.domain.mission.repository.MissionSetRepository;
import com.snow.popin.domain.mission.repository.UserMissionRepository;
import com.snow.popin.global.exception.MissionException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MissionSetService {

    private final MissionSetRepository missionSetRepository;
    private final UserMissionRepository userMissionRepository;

    public MissionSetService(MissionSetRepository missionSetRepository,
                             UserMissionRepository userMissionRepository) {
        this.missionSetRepository = missionSetRepository;
        this.userMissionRepository = userMissionRepository;
    }

    public MissionSetViewDto getOne(UUID missionSetId, Long userId) {
        MissionSet ms = missionSetRepository.findById(missionSetId)
                .orElseThrow(MissionException.MissionSetNotFound::new);

        return toViewDto(ms, userId);
    }

    // MissionSet 엔티티를 화면 표시용 DTO (MissionSetViewDto)로 변환
    private MissionSetViewDto toViewDto(MissionSet set, Long userId) {
        if (set == null) {
            throw new MissionException.MissionSetNotFound();
        }

        List<MissionSummaryDto> missions =
                Optional.ofNullable(set.getMissions()).orElse(Collections.emptyList())
                        .stream()
                        .map(m -> MissionSummaryDto.builder()
                                .id(m.getId())
                                .title(m.getTitle())
                                .description(m.getDescription())
                                .build())
                        .collect(Collectors.toList());

        long successCnt = 0L;
        Map<UUID, UserMissionStatus> statusByMission = new HashMap<>();

        if (userId != null) {
            List<UserMission> ums = userMissionRepository
                    .findByUser_IdAndMission_MissionSet_Id(userId, set.getId());

            if (ums == null) {
                throw new MissionException.MissionNotFound();
            }

            for (UserMission um : ums) {
                if (um.getMission() != null) {
                    statusByMission.put(um.getMission().getId(), um.getStatus());
                }
                if (um.getStatus() == UserMissionStatus.COMPLETED) {
                    successCnt++;
                }
            }

            // userStatus 반영
            missions = missions.stream()
                    .map(ms -> MissionSummaryDto.builder()
                            .id(ms.getId())
                            .title(ms.getTitle())
                            .description(ms.getDescription())
                            .userStatus(statusByMission.get(ms.getId()))
                            .build())
                    .collect(Collectors.toList());
        }

        int req = (set.getRequiredCount() == null ? 0 : set.getRequiredCount());
        boolean cleared = successCnt >= req;

        return MissionSetViewDto.from(set, userId, missions, successCnt, cleared);

    }
}
