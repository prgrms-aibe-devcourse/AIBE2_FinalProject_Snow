package com.snow.popin.domain.popup.repository;

import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long> {

    // 전체 팝업 조회 (상태별)
    Page<Popup> findByStatusOrderByCreatedAtDesc(PopupStatus status, Pageable pageable);

    // 모든 상태의 팝업 조회
    Page<Popup> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 팝업 상세 조회 (이미지, 시간 정보 포함)
    @EntityGraph(attributePaths = {"images", "hours"})
    @Query("SELECT p FROM Popup p WHERE p.id = :id")
    Optional<Popup> findByIdWithDetails(@Param("id") Long id);

    // 팝업 검색 (제목, 지역으로만)
    @Query("SELECT DISTINCT p FROM Popup p LEFT JOIN p.venue v " +
            "WHERE (:title IS NULL OR TRIM(:title) = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:region IS NULL OR TRIM(:region) = '' OR LOWER(v.region) = LOWER(:region))")
    Page<Popup> searchPopups(
            @Param("title") String title,
            @Param("region") String region,
            Pageable pageable);

    // 태그로 팝업 검색 (제목, 지역 조건 포함)
    @Query("SELECT DISTINCT p FROM Popup p " +
            "LEFT JOIN p.venue v " +
            "JOIN p.tags t " +
            "WHERE t.name IN :tagNames " +
            "AND (:title IS NULL OR TRIM(:title) = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:region IS NULL OR TRIM(:region) = '' OR LOWER(v.region) = LOWER(:region))")
    Page<Popup> searchPopupsByTags(
            @Param("tagNames") List<String> tagNames,
            @Param("title") String title,
            @Param("region") String region,
            Pageable pageable);

    // venue별 팝업 조회
    @Query("SELECT p FROM Popup p WHERE p.venue.id = :venueId ORDER BY p.createdAt DESC")
    Page<Popup> findByVenueId(@Param("venueId") Long venueId, Pageable pageable);

    // 특정 지역의 팝업 조회
    @Query("SELECT p FROM Popup p LEFT JOIN p.venue v WHERE v.region = :region ORDER BY p.createdAt DESC")
    Page<Popup> findByRegion(@Param("region") String region, Pageable pageable);

    // 주차 가능한 venue의 팝업 조회
    @Query("SELECT p FROM Popup p LEFT JOIN p.venue v WHERE v.parkingAvailable = true ORDER BY p.createdAt DESC")
    Page<Popup> findByParkingAvailable(Pageable pageable);
}