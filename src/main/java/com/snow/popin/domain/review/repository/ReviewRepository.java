package com.snow.popin.domain.review.repository;

import com.snow.popin.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 팝업의 차단되지 않은 리뷰 조회 (최신순, 페이징)
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.user u " +
            "WHERE r.popupId = :popupId AND r.isBlocked = false " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByPopupIdAndNotBlockedOrderByCreatedAtDesc(@Param("popupId") Long popupId, Pageable pageable);

    // 특정 팝업의 최근 리뷰 N개 조회 (상세페이지용)
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.user u " +
            "WHERE r.popupId = :popupId AND r.isBlocked = false " +
            "ORDER BY r.createdAt DESC")
    List<Review> findTopRecentReviewsByPopupId(@Param("popupId") Long popupId, Pageable pageable);

    // 사용자별 리뷰 조회
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.popup p " +
            "WHERE r.userId = :userId " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // 특정 팝업의 평점 통계
    @Query("SELECT AVG(r.rating), COUNT(r) FROM Review r " +
            "WHERE r.popupId = :popupId AND r.isBlocked = false")
    Object[] findRatingStatsByPopupId(@Param("popupId") Long popupId);

    // 사용자가 특정 팝업에 리뷰를 작성했는지 확인
    boolean existsByPopupIdAndUserId(Long popupId, Long userId);

    // 사용자의 특정 팝업 리뷰 조회
    Optional<Review> findByPopupIdAndUserId(Long popupId, Long userId);

    // 차단되지 않은 리뷰 개수 조회
    long countByPopupIdAndIsBlockedFalse(Long popupId);
}