package com.snow.popin.domain.popup.repository;

import com.snow.popin.domain.popup.entity.Popup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PopupSearchRepository extends JpaRepository<Popup, Long> {

    // 제목과 태그로 검색
    @Query(value = "SELECT DISTINCT p FROM Popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN p.tags t " +
            "WHERE (:query IS NULL OR TRIM(:query) = '' OR " +
            "     LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "     LOWER(p.summary) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "     LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY p.createdAt DESC",
            countQuery = "SELECT count(DISTINCT p) FROM Popup p " +
                    "LEFT JOIN p.tags t " +
                    "WHERE (:query IS NULL OR TRIM(:query) = '' OR " +
                    "     LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                    "     LOWER(p.summary) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                    "     LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Popup> searchByTitleAndTags(@Param("query") String query, Pageable pageable);

    //TODO: 추천 검색어
}