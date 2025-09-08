package com.snow.popin.domain.mission.repository;

import com.snow.popin.domain.mission.entity.MissionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MissionSetRepository extends JpaRepository<MissionSet, UUID> {
    List<MissionSet> findByPopupId(Long popupId);
}
