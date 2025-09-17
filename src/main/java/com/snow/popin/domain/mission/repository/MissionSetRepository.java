package com.snow.popin.domain.mission.repository;

import com.snow.popin.domain.mission.entity.MissionSet;
import com.snow.popin.domain.mission.entity.UserMission;
import com.snow.popin.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MissionSetRepository extends JpaRepository<MissionSet, UUID> {
    Page<MissionSet> findByPopupId(Long popupId, Pageable pageable);
    Page<MissionSet> findByStatus(String status, Pageable pageable);


    @Query("select um from UserMission um " +
            "join fetch um.mission m " +
            "join fetch m.missionSet ms " +
            "where um.user = :user")
    List<UserMission> findAllByUserWithMissionSet(User user);

}
