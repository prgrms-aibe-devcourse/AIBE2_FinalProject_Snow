package com.example.popin.domain.mission.service;

import com.example.popin.domain.mission.dto.MissionSetViewDto;
import com.example.popin.domain.mission.dto.MissionSummaryDto;
import com.example.popin.domain.mission.entity.MissionSet;
import com.example.popin.domain.mission.repository.MissionSetRepository;
import com.example.popin.domain.mission.entity.UserMission;
import com.example.popin.domain.mission.repository.UserMissionRepository;
import com.example.popin.domain.mission.entity.UserMissionStatus;
import com.example.popin.domain.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

@Service
public class MissionSetService {

    private final MissionSetRepository missionSetRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserService userService; // ★ 추가

    public MissionSetService(MissionSetRepository missionSetRepository,
                             UserMissionRepository userMissionRepository,
                             UserService userService) { // ★ 생성자에 주입
        this.missionSetRepository = missionSetRepository;
        this.userMissionRepository = userMissionRepository;
        this.userService = userService;
    }

    // 로그인 사용자 ID 해석 (로그인 안 되어 있으면 null)
    private Long resolveCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (principal == null || "anonymousUser".equals(principal)) return null;
            String username = auth.getName();
            return userService.getUserIdByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }

    /** 특정 팝업에 속한 미션셋 목록 조회 (+선택적으로 userId 진행도 포함) */
    @Transactional(readOnly = true)
    public List<MissionSetViewDto> getByPopup(Long popupId, Long userId) {
        if (userId == null) {
            userId = resolveCurrentUserId();
        }

        List<MissionSet> sets = missionSetRepository.findByPopupId(popupId);
        List<MissionSetViewDto> result = new ArrayList<MissionSetViewDto>();

        for (MissionSet set : sets) {
            MissionSetViewDto dto = new MissionSetViewDto();
            dto.setMissionSetId(set.getId());
            dto.setPopupId(set.getPopupId());
            dto.setRequiredCount(set.getRequiredCount());

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

                Map<UUID, UserMissionStatus> byMissionId = new HashMap<UUID, UserMissionStatus>();
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
                int req = (dto.getRequiredCount() == null ? 0 : dto.getRequiredCount().intValue());
                dto.setCleared(successCnt >= req);
            }

            result.add(dto);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<MissionSetViewDto> getByPopup(Long popupId) {
        return getByPopup(popupId, null);
    }
}
