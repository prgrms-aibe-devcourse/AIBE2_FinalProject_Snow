package com.snow.popin.domain.map.repository;

import com.snow.popin.domain.map.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MapRepository extends JpaRepository<Venue, Long> {

    //현재 진행중이거나 예정인 팝업이 있는 지역 목록 조회 (중복 제거)
    @Query("SELECT DISTINCT v.region FROM Venue v " +
            "WHERE v.region IS NOT NULL " +
            "AND EXISTS (SELECT 1 FROM Popup p WHERE p.venue = v " +
            "            AND p.status IN ('ONGOING', 'PLANNED')) " +
            "ORDER BY v.region")
    List<String> findDistinctRegionsWithActivePopups();

    //좌표가 있는 장소들의 지역 목록 조회
    @Query("SELECT DISTINCT v.region FROM Venue v " +
            "WHERE v.region IS NOT NULL " +
            "AND v.latitude IS NOT NULL " +
            "AND v.longitude IS NOT NULL " +
            "ORDER BY v.region")
    List<String> findDistinctRegionsWithCoordinates();
}
