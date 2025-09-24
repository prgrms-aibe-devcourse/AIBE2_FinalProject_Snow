package com.snow.popin.domain.popup.repository;

import com.snow.popin.domain.popup.entity.Popup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopupSearchRepository extends JpaRepository<Popup, Long> {

    /**
     * 팝업 제목과 태그로 검색 (2글자 이상)
     */
    @Query("SELECT DISTINCT p FROM Popup p " +
            "LEFT JOIN p.tags t " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY p.createdAt DESC")
    Page<Popup> searchByTitleAndTags(@Param("query") String query, Pageable pageable);

    /**
     * 자동완성 - 제목과 태그에서 검색어 포함된 것들 통합 조회
     */
    @Query(value = "(" +
            "SELECT DISTINCT p.title as suggestion FROM popups p " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "UNION " +
            "SELECT DISTINCT t.name as suggestion FROM popups p " +
            "JOIN popup_tags pt ON p.id = pt.popup_id " +
            "JOIN tags t ON pt.tag_id = t.id " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ") ORDER BY LENGTH(suggestion), suggestion " +
            "LIMIT :limit", nativeQuery = true)
    List<String> findSuggestions(@Param("query") String query, @Param("limit") int limit);
}