package com.example.popin.domain.missionset;

import com.example.popin.domain.missionset.dto.MissionSetViewDto;
import com.example.popin.domain.missionset.dto.MissionSummaryDto;
import com.example.popin.domain.usermission.UserMission;
import com.example.popin.domain.usermission.UserMissionRepository;
import com.example.popin.domain.usermission.UserMissionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MissionSetService {

    private final MissionSetRepository missionSetRepository;
    private final UserMissionRepository userMissionRepository;

    public MissionSetService(MissionSetRepository missionSetRepository,
                             UserMissionRepository userMissionRepository) {
        this.missionSetRepository = missionSetRepository;
        this.userMissionRepository = userMissionRepository;
    }

    /**
     * 특정 팝업에 속한 미션셋 목록 조회 (+선택적으로 userId 진행도 포함)
     */
    @Transactional(readOnly = true)
    public List<MissionSetViewDto> getByPopup(Long popupId, Long userId) {
        List<MissionSet> sets = missionSetRepository.findByPopupId(popupId);
        List<MissionSetViewDto> result = new ArrayList<MissionSetViewDto>();

        for (MissionSet set : sets) {
            MissionSetViewDto dto = new MissionSetViewDto();
            dto.setMissionSetId(set.getId());
            dto.setPopupId(set.getPopupId());
            dto.setRequiredCount(set.getRequiredCount());

            // 미션 요약 목록 (정답은 절대 포함 X)
            List<MissionSummaryDto> missions = new ArrayList<MissionSummaryDto>();
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

            if (userId != null) {
                dto.setUserId(userId);

                List<UserMission> ums = userMissionRepository
                        .findByUser_IdAndMission_MissionSet_Id(userId, set.getId());

                Map<Long, UserMissionStatus> byMissionId = new HashMap<Long, UserMissionStatus>();
                long successCnt = 0L;

                for (UserMission um : ums) {
                    Long missionId = (um.getMission() != null ? um.getMission().getId() : null);
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
                int req = (dto.getRequiredCount() == null ? 0 : dto.getRequiredCount().intValue());
                dto.setCleared(successCnt >= req);
            }

            result.add(dto);
        }

        return result;
    }
}
