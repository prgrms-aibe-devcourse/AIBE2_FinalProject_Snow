package com.example.popin.domain.usermission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    Optional<UserMission> findByUser_IdAndMission_Id(Long userId, Long missionId);
    long countByUser_IdAndMission_MissionSet_IdAndStatus(Long userId, Long missionSetId, UserMissionStatus status);
}
