package com.snow.popin.domain.bookmark.repository;

import com.snow.popin.domain.bookmark.entity.BookMark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Long> {

    // 사용자별 북마크 목록 조회 (페이징)
    @Query(value = "SELECT b FROM BookMark b " +
            "LEFT JOIN FETCH b.popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE b.userId = :userId " +
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(b) FROM BookMark b WHERE b.userId = :userId")
    Page<BookMark> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // 사용자별 북마크 목록 조회 (리스트)
    @Query("SELECT b FROM BookMark b " +
            "LEFT JOIN FETCH b.popup p " +
            "LEFT JOIN FETCH p.venue v " +
            "WHERE b.userId = :userId " +
            "ORDER BY b.createdAt DESC")
    List<BookMark> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 특정 사용자의 특정 팝업 북마크 여부 확인
    boolean existsByUserIdAndPopupId(Long userId, Long popupId);

    // 사용자별 북마크 수 조회
    long countByUserId(Long userId);

    // 팝업별 북마크 수 조회
    long countByPopupId(Long popupId);

    // 사용자의 북마크한 팝업 ID 목록 조회
    @Query("SELECT b.popupId FROM BookMark b WHERE b.userId = :userId")
    List<Long> findPopupIdsByUserId(@Param("userId") Long userId);

    // 사용자의 특정 팝업 북마크 삭제
    @Modifying
    @Query("DELETE FROM BookMark b WHERE b.userId = :userId AND b.popupId = :popupId")
    void deleteByUserIdAndPopupId(@Param("userId") Long userId, @Param("popupId") Long popupId);
}