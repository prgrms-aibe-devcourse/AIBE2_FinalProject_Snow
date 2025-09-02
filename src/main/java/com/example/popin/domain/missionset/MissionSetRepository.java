package com.example.popin.domain.missionset;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MissionSetRepository extends JpaRepository<MissionSet, Long> {
    List<MissionSet> findByPopupId(Long popupId);
}
