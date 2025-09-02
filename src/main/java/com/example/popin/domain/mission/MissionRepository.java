package com.example.popin.domain.mission;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByMissionSet_Id(Long missionSetId);
}
