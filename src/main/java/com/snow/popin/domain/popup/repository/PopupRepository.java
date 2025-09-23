package com.snow.popin.domain.popup.repository;

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

    // ===== 메인 페이지 필터링용 메서드들 =====

    // 전체 팝업 조회 (상태별 필터링)
    @Query(value = "SELECT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "ORDER BY p.createdAt DESC",
            countQuery = "SELECT count(p) FROM Popup p " +
                    "WHERE (:status IS NULL OR p.status = :status)")
    Page<Popup> findAllWithStatusFilter(@Param("status") PopupStatus status, Pageable pageable);

    // 인기 팝업 조회 (조회수 기준)
    @Query(value = "SELECT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "ORDER BY p.viewCount DESC, p.createdAt DESC",
            countQuery = "SELECT count(p) FROM Popup p " +
                    "WHERE (:status IS NULL OR p.status = :status)")
    Page<Popup> findPopularByViewCount(@Param("status") PopupStatus status, Pageable pageable);

    // 마감임박 팝업 조회
    @Query(value = "SELECT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "AND p.endDate >= CURRENT_DATE " +
            "AND p.endDate <= CURRENT_DATE + 7 " +
            "ORDER BY p.endDate ASC",
            countQuery = "SELECT count(p) FROM Popup p " +
                    "WHERE (:status IS NULL OR p.status = :status) " +
                    "AND p.endDate >= CURRENT_DATE " +
                    "AND p.endDate <= CURRENT_DATE + 7 " +
                    "AND p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING")
    Page<Popup> findDeadlineSoonPopups(@Param("status") PopupStatus status, Pageable pageable);

    // 지역별 + 기간별 필터링 (가장 범용적 - 다른 메서드들 대체)
    @Query(value = "SELECT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE (:region IS NULL OR :region = '전체' OR v.region LIKE CONCAT('%', :region, '%')) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:startDate IS NULL OR p.startDate <= :endDate) " +
            "AND (:endDate IS NULL OR p.endDate >= :startDate) " +
            "ORDER BY p.createdAt DESC",
            countQuery = "SELECT count(p) FROM Popup p " +
                    "LEFT JOIN p.venue v " +
                    "WHERE (:region IS NULL OR :region = '전체' OR v.region LIKE CONCAT('%', :region, '%')) " +
                    "AND (:status IS NULL OR p.status = :status) " +
                    "AND (:startDate IS NULL OR p.startDate <= :endDate) " +
                    "AND (:endDate IS NULL OR p.endDate >= :startDate)")
    Page<Popup> findByRegionAndDateRange(
            @Param("region") String region,
            @Param("status") PopupStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // ===== 팝업 상세 조회 =====

    @EntityGraph(attributePaths = {"images", "hours", "venue", "tags", "category"})
    @Query("SELECT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE p.id = :id")
    Optional<Popup> findByIdWithDetails(@Param("id") Long id);

    // ===== 유사/추천 팝업 조회 =====

    @Query(value = "SELECT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE c.name = :categoryName " +
            "AND p.id != :excludeId " +
            "AND (p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING OR " +
            "     p.status = com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "ORDER BY p.viewCount DESC, p.createdAt DESC",  // 조회수 기준으로 수정
            countQuery = "SELECT count(p) FROM Popup p " +
                    "LEFT JOIN p.category c " +
                    "WHERE c.name = :categoryName " +
                    "AND p.id != :excludeId " +
                    "AND (p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING OR " +
                    "     p.status = com.snow.popin.domain.popup.entity.PopupStatus.PLANNED)")
    Page<Popup> findSimilarPopups(@Param("categoryName") String categoryName,
                                  @Param("excludeId") Long excludeId,
                                  Pageable pageable);

    @Query(value = "SELECT p FROM Popup p LEFT JOIN FETCH p.venue v LEFT JOIN FETCH p.category c " +
            "WHERE c.id IN :categoryIds " +
            "AND p.status IN (com.snow.popin.domain.popup.entity.PopupStatus.ONGOING, com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "ORDER BY p.viewCount DESC, p.createdAt DESC",  // 조회수 기준으로 수정
            countQuery = "SELECT count(p) FROM Popup p " +
                    "WHERE p.category.id IN :categoryIds " +
                    "AND p.status IN (com.snow.popin.domain.popup.entity.PopupStatus.ONGOING, com.snow.popin.domain.popup.entity.PopupStatus.PLANNED)")
    Page<Popup> findRecommendedPopupsByCategories(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    // ===== 카테고리/지역별 조회 =====

    @Query(value = "SELECT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE (:categoryName IS NULL OR c.name = :categoryName) " +
            "AND (p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING OR " +
            "     p.status = com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "ORDER BY p.viewCount DESC, p.createdAt DESC",  // 조회수 기준으로 수정
            countQuery = "SELECT count(p) FROM Popup p " +
                    "LEFT JOIN p.category c " +
                    "WHERE (:categoryName IS NULL OR c.name = :categoryName) " +
                    "AND (p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING OR " +
                    "     p.status = com.snow.popin.domain.popup.entity.PopupStatus.PLANNED)")
    Page<Popup> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE v.region = :region " +
            "AND (p.status = com.snow.popin.domain.popup.entity.PopupStatus.ONGOING OR " +
            "     p.status = com.snow.popin.domain.popup.entity.PopupStatus.PLANNED) " +
            "ORDER BY p.viewCount DESC, p.createdAt DESC")  // 조회수 기준으로 수정
    List<Popup> findByRegion(@Param("region") String region);

    // ===== 지도용 메서드들 =====

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

    // ===== 통계용 메서드들 =====

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

    // ===== 관리용 메서드들 =====

    List<Popup> findByBrandId(Long brandId);

    Optional<Popup> findFirstByTitle(String title);

    @Query("SELECT p FROM Popup p WHERE p.status != com.snow.popin.domain.popup.entity.PopupStatus.ONGOING AND p.startDate <= :today AND p.endDate >= :today")
    List<Popup> findPopupsToUpdateToOngoing(@Param("today") LocalDate today);

    @Query("SELECT p FROM Popup p WHERE p.status != com.snow.popin.domain.popup.entity.PopupStatus.ENDED AND p.endDate < :today")
    List<Popup> findPopupsToUpdateToEnded(@Param("today") LocalDate today);

    // ===== 통계용 기본 메서드들 =====

    long countByStatus(PopupStatus status);
    long count();
}