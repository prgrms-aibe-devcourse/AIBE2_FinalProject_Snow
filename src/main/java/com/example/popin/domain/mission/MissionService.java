package com.example.popin.domain.mission;

import com.example.popin.domain.mission.Mission;
import com.example.popin.domain.mission.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepository;

    public Mission create(Mission mission) {
        return missionRepository.save(mission);
    }

    public Optional<Mission> findById(UUID id) {
        return missionRepository.findById(id);
    }
}
