package com.snow.popin.domain.admin.controller;

import com.snow.popin.domain.mission.dto.MissionCreateRequestDto;
import com.snow.popin.domain.mission.dto.MissionDto;
import com.snow.popin.domain.mission.dto.MissionSetAdminDto;
import com.snow.popin.domain.mission.dto.MissionSetCreateRequestDto;
import com.snow.popin.domain.admin.service.AdminMissionService;
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

    private final AdminMissionService missionAdminService;

    @GetMapping
    public Page<MissionSetAdminDto> list(Pageable pageable,
                                         @RequestParam(required = false) Long popupId,
                                         @RequestParam(required = false) String status) {
        return missionAdminService.getMissionSets(pageable, popupId, status);
    }

    // 미션셋 상세
    @GetMapping("/{id}")
    public MissionSetAdminDto detail(@PathVariable UUID id) {
        return missionAdminService.getMissionSetDetail(id);
    }

    // 미션셋 생성
    @PostMapping
    public MissionSetAdminDto create(@RequestBody MissionSetCreateRequestDto request) {
        return missionAdminService.createMissionSet(request);
    }

    // 미션셋 완료 처리
    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> complete(@PathVariable UUID id) {
        missionAdminService.completeMissionSet(id);
        return ResponseEntity.ok().build();
    }

    // 미션셋 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        missionAdminService.deleteMissionSet(id);
        return ResponseEntity.ok().build();
    }

    // 미션 추가
    @PostMapping("/{id}/missions")
    public MissionDto addMission(@PathVariable UUID id, @RequestBody MissionCreateRequestDto request) {
        return missionAdminService.addMission(id, request);
    }

    // 미션 삭제
    @DeleteMapping("/missions/{missionId}")
    public ResponseEntity<Void> deleteMission(@PathVariable UUID missionId) {
        missionAdminService.deleteMission(missionId);
        return ResponseEntity.ok().build();
    }
}
