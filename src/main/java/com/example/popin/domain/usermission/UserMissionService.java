package com.example.popin.domain.usermission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserMissionService {
    private final UserMissionRepository userMissionRepository;

    public UserMission create(UserMission userMission) {
        return userMissionRepository.save(userMission);
    }

    public Optional<UserMission> findById(Long id) {
        return userMissionRepository.findById(id);
    }
}


