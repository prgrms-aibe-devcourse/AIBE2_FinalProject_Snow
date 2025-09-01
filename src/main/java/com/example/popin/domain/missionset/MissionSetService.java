package com.example.popin.domain.missionset;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MissionSetService {
    private final MissionSetRepository missionSetRepository;

    public MissionSet create(MissionSet missionSet) {
        return missionSetRepository.save(missionSet);
    }

    public Optional<MissionSet> findById(Long id) {
        return missionSetRepository.findById(id);
    }
}
