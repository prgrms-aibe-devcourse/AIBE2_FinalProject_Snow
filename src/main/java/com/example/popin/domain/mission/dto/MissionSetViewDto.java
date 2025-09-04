package com.example.popin.domain.mission.dto;

import com.example.popin.domain.mission.entity.MissionSet;
import com.example.popin.domain.mission.entity.UserMission;
import com.example.popin.domain.mission.entity.UserMissionStatus;
import com.example.popin.domain.mission.repository.UserMissionRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter @Setter
public class MissionSetViewDto {
    private UUID missionSetId;
    private Long popupId;
    private Integer requiredCount;
    private List<MissionSummaryDto> missions;
    private int totalMissions;

    private Long userId;
    private Long successCount;
    private Boolean cleared;

    public static MissionSetViewDto of(MissionSet set, Long userId, UserMissionRepository userMissionRepository) {
        MissionSetViewDto dto = new MissionSetViewDto();
        dto.setMissionSetId(set.getId());
        dto.setPopupId(set.getPopupId());
        dto.setRequiredCount(set.getRequiredCount());

        // 미션 목록 변환
        List<MissionSummaryDto> missions = new ArrayList<>();
        if (set.getMissions() != null) {
            set.getMissions().forEach(m -> {
                MissionSummaryDto ms = new MissionSummaryDto();
                ms.setId(m.getId());
                ms.setTitle(m.getTitle());
                ms.setDescription(m.getDescription());
                missions.add(ms);
            });
        }
        dto.setMissions(missions);
        dto.setTotalMissions(missions.size());

        // 사용자 진행도 반영
        if (userId != null) {
            dto.setUserId(userId);

            List<UserMission> ums = userMissionRepository.findByUser_IdAndMission_MissionSet_Id(userId, set.getId());

            Map<UUID, UserMissionStatus> byMissionId = new HashMap<>();
            long successCnt = 0L;

            for (UserMission um : ums) {
                UUID missionId = (um.getMission() != null ? um.getMission().getId() : null);
                if (missionId != null) {
                    byMissionId.put(missionId, um.getStatus());
                }
                if (um.getStatus() == UserMissionStatus.SUCCESS) {
                    successCnt++;
                }
            }

            for (MissionSummaryDto ms : missions) {
                ms.setUserStatus(byMissionId.get(ms.getId()));
            }

            dto.setSuccessCount(successCnt);
            int req = (dto.getRequiredCount() == null ? 0 : dto.getRequiredCount());
            dto.setCleared(successCnt >= req);
        }

        return dto;
    }
}
