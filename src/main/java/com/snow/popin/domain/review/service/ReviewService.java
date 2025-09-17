package com.snow.popin.domain.review.service;

import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.review.dto.*;
import com.snow.popin.domain.review.entity.Review;
import com.snow.popin.domain.review.repository.ReviewRepository;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PopupRepository popupRepository;
    private final UserRepository userRepository;

    // 리뷰 작성
    @Transactional
    public ReviewResponseDto createReview(Long userId, ReviewCreateRequestDto request) {
        // 팝업 존재 확인
        Popup popup = popupRepository.findById(request.getPopupId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팝업입니다."));

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 중복 리뷰 확인
        if (reviewRepository.existsByPopupIdAndUserId(request.getPopupId(), userId)) {
            throw new IllegalArgumentException("이미 해당 팝업에 리뷰를 작성했습니다.");
        }

        // 리뷰 생성 및 저장
        Review review = request.toEntity(userId);
        Review savedReview = reviewRepository.save(review);

        // 연관관계 설정 (조회용)
        savedReview = reviewRepository.findById(savedReview.getId()).orElseThrow();

        log.info("리뷰 작성 완료 - 사용자: {}, 팝업: {}, 평점: {}",
                user.getName(), popup.getTitle(), request.getRating());

        return ReviewResponseDto.from(savedReview);
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, Long userId, ReviewUpdateRequestDto request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 수정 권한 확인
        if (!review.canEdit(userId)) {
            throw new IllegalArgumentException("리뷰 수정 권한이 없습니다.");
        }

        // 내용 수정
        review.updateContent(request.getContent());
        review.updateRating(request.getRating());

        log.info("리뷰 수정 완료 - 리뷰ID: {}, 사용자ID: {}", reviewId, userId);

        return ReviewResponseDto.from(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 삭제 권한 확인
        if (!review.canEdit(userId)) {
            throw new IllegalArgumentException("리뷰 삭제 권한이 없습니다.");
        }

        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료 - 리뷰ID: {}, 사용자ID: {}", reviewId, userId);
    }

    // 팝업의 최근 리뷰 조회 (상세페이지용 - 최대 2개)
    @Transactional(readOnly = true)
    public List<ReviewListResponseDto> getRecentReviewsByPopup(Long popupId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findTopRecentReviewsByPopupId(popupId, pageable);

        return reviews.stream()
                .map(ReviewListResponseDto::from)
                .collect(Collectors.toList());
    }

    // 팝업의 전체 리뷰 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<ReviewListResponseDto> getReviewsByPopup(Long popupId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByPopupIdAndNotBlockedOrderByCreatedAtDesc(popupId, pageable);

        return reviewPage.map(ReviewListResponseDto::from);
    }

    // 사용자 리뷰 목록 조회
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByUser(Long userId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return reviewPage.map(ReviewResponseDto::from);
    }

    // 팝업 리뷰 통계 조회
    @Transactional(readOnly = true)
    public ReviewStatsDto getReviewStats(Long popupId) {
        Object[] result = reviewRepository.findRatingStatsByPopupId(popupId);
        return ReviewStatsDto.from(result);
    }

    // 사용자가 해당 팝업에 리뷰 작성 여부 확인
    @Transactional(readOnly = true)
    public boolean hasUserReviewedPopup(Long popupId, Long userId) {
        return reviewRepository.existsByPopupIdAndUserId(popupId, userId);
    }

    // 사용자의 특정 팝업 리뷰 조회
    @Transactional(readOnly = true)
    public ReviewResponseDto getUserReviewForPopup(Long popupId, Long userId) {
        Review review = reviewRepository.findByPopupIdAndUserId(popupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팝업에 작성한 리뷰가 없습니다."));

        return ReviewResponseDto.from(review);
    }

    // 리뷰 단건 조회
    @Transactional(readOnly = true)
    public ReviewResponseDto getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        return ReviewResponseDto.from(review);
    }
}