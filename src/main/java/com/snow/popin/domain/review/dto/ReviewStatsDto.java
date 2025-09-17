package com.snow.popin.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

// 리뷰 통계 DTO
@Getter
public class ReviewStatsDto {

    private Double averageRating;
    private Long totalReviews;

    @Builder
    public ReviewStatsDto(Double averageRating, Long totalReviews) {
        this.averageRating = averageRating != null ? Math.round(averageRating * 10) / 10.0 : 0.0;
        this.totalReviews = totalReviews != null ? totalReviews : 0L;
    }

    public static ReviewStatsDto from(Object[] result) {
        if (result == null || result.length < 2) {
            return ReviewStatsDto.builder().build();
        }

        Double avgRating = result[0] != null ? (Double) result[0] : 0.0;
        Long count = result[1] != null ? (Long) result[1] : 0L;

        return ReviewStatsDto.builder()
                .averageRating(avgRating)
                .totalReviews(count)
                .build();
    }
}