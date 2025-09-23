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

    // 제목과 태그로 검색
    @Query(value = "SELECT DISTINCT p FROM Popup p " +
            "LEFT JOIN p.venue v " +
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

    // 자동완성용: 팝업 제목에서 검색어가 포함된 제목들을 찾기
    @Query("SELECT DISTINCT p.title FROM Popup p " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY LENGTH(p.title), p.title")
    List<String> findPopupTitleSuggestions(@Param("query") String query, Pageable pageable);

    // 자동완성용: 태그에서 검색어가 포함된 태그들을 찾기
    @Query("SELECT DISTINCT t.name FROM Popup p " +
            "LEFT JOIN p.tags t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY LENGTH(t.name), t.name")
    List<String> findTagSuggestions(@Param("query") String query, Pageable pageable);

    // 자동완성용: 통합 검색어 제안 (제목 + 태그, 모든 상태)
    @Query(value = "(" +
            "SELECT p.title as suggestion, 'title' as type, p.view_count as popularity " +
            "FROM popups p " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            ") UNION ALL (" +
            "SELECT t.name as suggestion, 'tag' as type, COUNT(pt.popup_id) as popularity " +
            "FROM tags t " +
            "LEFT JOIN popup_tags pt ON t.id = pt.tag_id " +
            "LEFT JOIN popups p ON pt.popup_id = p.id " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "GROUP BY t.name " +
            ") ORDER BY popularity DESC, LENGTH(suggestion), suggestion " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findSuggestions(@Param("query") String query, @Param("limit") int limit);
}