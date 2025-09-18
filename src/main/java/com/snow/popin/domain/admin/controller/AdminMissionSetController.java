package com.snow.popin.domain.admin.controller;

import com.snow.popin.domain.mission.dto.*;
import com.snow.popin.domain.admin.service.AdminMissionService;
import com.snow.popin.domain.mission.entity.MissionSetStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/mission-sets")
public class AdminMissionSetController {

    private final AdminMissionService adminMissionService;

    @GetMapping
    public Page<MissionSetAdminDto> list(Pageable pageable,
                                         @RequestParam(required = false) Long popupId,
                                         @RequestParam(required = false) MissionSetStatus status) {
        return adminMissionService.getMissionSets(pageable, popupId, status);
    }

    /**
     * 미션셋 상세
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public MissionSetAdminDto detail(@PathVariable UUID id) {
        return adminMissionService.getMissionSetDetail(id);
    }

    /**
     * 미션셋 생성
     * @param request
     * @return
     */
    @PostMapping
    public MissionSetAdminDto create(@RequestBody MissionSetCreateRequestDto request) {
        return adminMissionService.createMissionSet(request);
    }

    /**
     * 미션셋 삭제
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        adminMissionService.deleteMissionSet(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 미션셋 수정
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public MissionSetAdminDto updateMissionSet(
            @PathVariable UUID id,
            @RequestBody MissionSetUpdateRequestDto request
    ) {
        return adminMissionService.updateMissionSet(id, request);
    }


    /**
     * 미션 추가
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/{id}/missions")
    public MissionDto addMission(@PathVariable UUID id, @RequestBody MissionCreateRequestDto request) {
        return adminMissionService.addMission(id, request);
    }

    /**
     * 미션 삭제
     * @param missionId
     * @return
     */
    @DeleteMapping("/missions/{missionId}")
    public ResponseEntity<Void> deleteMission(@PathVariable UUID missionId) {
        adminMissionService.deleteMission(missionId);
        return ResponseEntity.ok().build();
    }
}
