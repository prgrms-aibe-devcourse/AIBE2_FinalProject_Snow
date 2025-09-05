package com.snow.popin.domain.mission.service;

import com.snow.popin.domain.mission.entity.Mission;
import com.snow.popin.domain.mission.repository.MissionRepository;
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
