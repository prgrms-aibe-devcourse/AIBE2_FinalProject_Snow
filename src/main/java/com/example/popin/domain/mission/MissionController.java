package com.example.popin.domain.mission;

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
        return toDto(m);
    }

    @GetMapping
    public List<MissionDto> list(@RequestParam(required = false) UUID missionSetId) {
        List<Mission> list = (missionSetId == null)
                ? missionRepository.findAll()
                : missionRepository.findByMissionSet_Id(missionSetId);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    private MissionDto toDto(Mission m) {
        UUID msId = (m.getMissionSet() != null) ? m.getMissionSet().getId() : null;
        return new MissionDto(m.getId(), m.getTitle(), m.getDescription(), msId);
    }
}
