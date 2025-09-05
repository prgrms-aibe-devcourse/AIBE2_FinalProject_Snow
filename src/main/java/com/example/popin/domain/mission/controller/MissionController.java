package com.example.popin.domain.mission.controller;

import com.example.popin.domain.mission.entity.Mission;
import com.example.popin.domain.mission.repository.MissionRepository;
import com.example.popin.domain.mission.dto.MissionDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionRepository missionRepository;

    public MissionController(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @GetMapping("/{id}")
    public MissionDto get(@PathVariable UUID id) {
        Mission m = missionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("mission not found"));
        return MissionDto.from(m);
    }

    @GetMapping
    public List<MissionDto> list(@RequestParam(required = false) UUID missionSetId) {
        List<Mission> list = (missionSetId == null)
                ? missionRepository.findAll()
                : missionRepository.findByMissionSet_Id(missionSetId);
        return list.stream().map(MissionDto::from).collect(Collectors.toList());
    }


}
