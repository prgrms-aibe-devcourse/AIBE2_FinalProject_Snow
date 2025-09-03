package com.example.popin.domain.missionset;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MissionSetRepository extends JpaRepository<MissionSet, UUID> {
    List<MissionSet> findByPopupId(Long popupId);
}
