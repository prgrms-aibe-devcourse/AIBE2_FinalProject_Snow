package com.snow.popin.domain.popup.repository;

import com.fasterxml.classmate.TypeBindings;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long>, JpaSpecificationExecutor<Popup> {

    // 통합 필터링 쿼리
    @Query(value = "SELECT p FROM Popup p LEFT JOIN FETCH p.venue v " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "AND (:region IS NULL OR :region = '전체' OR v.region = :region) " +
            "AND (:startDate IS NULL OR p.endDate IS NULL OR p.endDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.startDate IS NULL OR p.startDate <= :endDate)",
            countQuery = "SELECT count(p) FROM Popup p LEFT JOIN p.venue v " +
                    "WHERE (:status IS NULL OR p.status = :status) " +
                    "AND (:region IS NULL OR :region = '전체' OR v.region = :region) " +
                    "AND (:startDate IS NULL OR p.endDate IS NULL OR p.endDate >= :startDate) " +
                    "AND (:endDate IS NULL OR p.startDate IS NULL OR p.startDate <= :endDate)")
    Page<Popup> findWithFilters(
            @Param("status") PopupStatus status,
            @Param("region") String region,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // 마감임박 팝업 (진행중이고 종료일 7일 이내)
    @Query(value = "SELECT p FROM Popup p LEFT JOIN FETCH p.venue v " +
            "WHERE p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING " +
            "AND p.endDate IS NOT NULL " +
            "AND p.endDate >= CURRENT_DATE " +
            "AND p.endDate <= :deadline " +
            "ORDER BY p.endDate ASC",
            countQuery = "SELECT count(p) FROM Popup p " +
                    "WHERE p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING " +
                    "AND p.endDate IS NOT NULL " +
                    "AND p.endDate >= CURRENT_DATE " +
                    "AND p.endDate <= :deadline")
    Page<Popup> findDeadlineSoonPopups(@Param("deadline") LocalDate deadline, Pageable pageable);

    // 팝업 상세 조회
    @EntityGraph(attributePaths = {"images", "hours", "venue", "tags", "category"})
    @Query("SELECT p FROM Popup p WHERE p.id = :id")
    Optional<Popup> findByIdWithDetails(@Param("id") Long id);

    // 인기 팝업 조회 (isFeatured = true)
    @Query(value = "SELECT p FROM Popup p LEFT JOIN FETCH p.venue v LEFT JOIN FETCH p.category c " +
            "WHERE p.isFeatured = true " +
            "AND p.status IN (com.snow.popin.domain.popup.entity.PopupStatus.ONGOING, com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "ORDER BY p.createdAt DESC",
            countQuery = "SELECT count(p) FROM Popup p " +
                    "WHERE p.isFeatured = true " +
                    "AND p.status IN (com.snow.popin.domain.popup.entity.PopupStatus.ONGOING, com.snow.popin.domain.popup.entity.PopupStatus.PLANNED)")
    Page<Popup> findPopularPopups(Pageable pageable);

    // 사용자 관심 카테고리 기반 추천 팝업 조회
    @Query(value = "SELECT p FROM Popup p LEFT JOIN FETCH p.venue v LEFT JOIN FETCH p.category c " +
            "WHERE c.id IN :categoryIds " +
            "AND p.status IN (com.snow.popin.domain.popup.entity.PopupStatus.ONGOING, com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "ORDER BY p.createdAt DESC",
            countQuery = "SELECT count(p) FROM Popup p " +
                    "WHERE p.category.id IN :categoryIds " +
                    "AND p.status IN (com.snow.popin.domain.popup.entity.PopupStatus.ONGOING, com.snow.popin.domain.popup.entity.PopupStatus.PLANNED)")
    Page<Popup> findRecommendedPopupsByCategories(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    // 사용자가 로그인하지 않은 경우 기본 추천 팝업 (최신순)
    @Query(value = "SELECT p FROM Popup p LEFT JOIN FETCH p.venue v LEFT JOIN FETCH p.category c " +
            "WHERE p.status IN (com.snow.popin.domain.popup.entity.PopupStatus.ONGOING, com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "ORDER BY p.createdAt DESC",
            countQuery = "SELECT count(p) FROM Popup p " +
                    "WHERE p.status IN (com.snow.popin.domain.popup.entity.PopupStatus.ONGOING, com.snow.popin.domain.popup.entity.PopupStatus.PLANNED)")
    Page<Popup> findDefaultRecommendedPopups(Pageable pageable);

    // 지도에 표시할 팝업 목록 조회 (좌표가 있고 진행중/예정인 팝업만)
    @Query("SELECT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE (p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING OR " +
            "       p.status = com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "AND v.latitude IS NOT NULL " +
            "AND v.longitude IS NOT NULL " +
            "AND (:region IS NULL OR :region = '전체' OR v.region = :region) " +
            "AND (:categoryIds IS NULL OR c.id IN :categoryIds) " +
            "ORDER BY p.createdAt DESC")
    List<Popup> findPopupsForMap(@Param("region") String region,
                                 @Param("categoryIds") List<Long> categoryIds);

    // 특정 좌표 범위 내의 팝업 조회 (바운딩 박스)
    @Query("SELECT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE (p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING OR " +
            "       p.status = com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "AND v.latitude IS NOT NULL " +
            "AND v.longitude IS NOT NULL " +
            "AND v.latitude BETWEEN :southWestLat AND :northEastLat " +
            "AND v.longitude BETWEEN :southWestLng AND :northEastLng " +
            "ORDER BY p.createdAt DESC")
    List<Popup> findPopupsInBounds(@Param("southWestLat") double southWestLat,
                                   @Param("southWestLng") double southWestLng,
                                   @Param("northEastLat") double northEastLat,
                                   @Param("northEastLng") double northEastLng);

    // 특정 지점 주변 팝업 조회 (반경 기반)
    @Query(value = "SELECT p.* " +
            "FROM popups p " +
            "INNER JOIN venues v ON p.venue_id = v.id " +
            "WHERE p.status IN ('ONGOING', 'PLANNED') " +
            "AND v.latitude IS NOT NULL " +
            "AND v.longitude IS NOT NULL " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(v.latitude)) * " +
            "     cos(radians(v.longitude) - radians(:lng)) + " +
            "     sin(radians(:lat)) * sin(radians(v.latitude)))) <= :radiusKm " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(v.latitude)) * " +
            "         cos(radians(v.longitude) - radians(:lng)) + " +
            "         sin(radians(:lat)) * sin(radians(v.latitude)))) ASC",
            nativeQuery = true)
    List<Popup> findPopupsWithinRadius(@Param("lat") double latitude,
                                       @Param("lng") double longitude,
                                       @Param("radiusKm") double radiusKm);

    // 카테고리별 지도 팝업 통계 조회
    @Query("SELECT c.name, COUNT(p) FROM Popup p " +
            "LEFT JOIN p.category c " +
            "LEFT JOIN p.venue v " +
            "WHERE (p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING OR " +
            "       p.status = com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "AND v.latitude IS NOT NULL " +
            "AND v.longitude IS NOT NULL " +
            "AND (:region IS NULL OR :region = '전체' OR v.region = :region) " +
            "GROUP BY c.id, c.name " +
            "ORDER BY COUNT(p) DESC")
    List<Object[]> findMapPopupStatsByCategory(@Param("region") String region);

    // 지역별 지도 팝업 통계 조회
    @Query("SELECT v.region, COUNT(p) FROM Popup p " +
            "LEFT JOIN p.venue v " +
            "WHERE (p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING OR " +
            "       p.status = com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "AND v.latitude IS NOT NULL " +
            "AND v.longitude IS NOT NULL " +
            "AND v.region IS NOT NULL " +
            "GROUP BY v.region " +
            "ORDER BY COUNT(p) DESC")
    List<Object[]> findMapPopupStatsByRegion();

    // 브랜드 ID를 기준으로 등록된 모든 팝업 조회
    List<Popup> findByBrandId(Long brandId);

    // title로 팝업 조회
    Optional<Popup> findFirstByTitle(String title);

    /**
     * 상태별 팝업 개수 조회
     */
    long countByStatus(PopupStatus status);

    // ONGOING 상태로 변경되어야 할 PLANNED 상태의 팝업 목록을 조회
    @Query("SELECT p FROM Popup p WHERE p.status = com.snow.popin.domain.popup.entity.PopupStatus.PLANNED AND p.startDate <= :today")
    List<Popup> findPopupsToUpdateToOngoing(@Param("today") LocalDate today);

    // ENDED 상태로 변경되어야 할 ONGOING 상태의 팝업 목록을 조회
    @Query("SELECT p FROM Popup p WHERE p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING AND p.endDate < :today")
    List<Popup> findPopupsToUpdateToEnded(@Param("today") LocalDate today);
}