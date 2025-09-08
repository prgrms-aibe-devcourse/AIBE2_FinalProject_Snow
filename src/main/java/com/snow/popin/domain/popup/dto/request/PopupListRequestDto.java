package com.snow.popin.domain.popup.dto.request;

import com.snow.popin.domain.popup.entity.PopupStatus;
import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.Min;
import java.time.LocalDate;

@Getter
public class PopupListRequestDto {

    // 상태 필터
    private PopupStatus status;

    // 지역 필터
    private String region;

    // 날짜 필터 타입 (today, week, two_weeks, custom)
    private String dateFilter;

    // 사용자 직접 선택 날짜 (dateFilter="custom"일 때만)
    private LocalDate startDate;
    private LocalDate endDate;

    // 정렬 방식 (latest=최신순, deadline=마감순, date=날짜순)
    private String sortBy = "latest";

    // 페이징
    @Min(0)
    private int page = 0;

    @Min(1)
    private int size = 20;

    // === Setters ===
    public void setStatus(PopupStatus status) {
        this.status = status;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setDateFilter(String dateFilter) {
        this.dateFilter = dateFilter;
        // 날짜 필터에 따라 자동으로 날짜 범위 설정
        if (dateFilter != null && !"custom".equals(dateFilter)) {
            LocalDate now = LocalDate.now();
            switch (dateFilter) {
                case "today":
                    this.startDate = now;
                    this.endDate = now;
                    break;
                case "week":
                    this.startDate = now;
                    this.endDate = now.plusDays(7);
                    break;
                case "two_weeks":
                    this.startDate = now;
                    this.endDate = now.plusDays(14);
                    break;
                default:
                    this.startDate = null;
                    this.endDate = null;
            }
        }
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    // === Helper Methods ===

    /**
     * 마감임박 필터 여부 확인
     */
    public boolean isDeadlineSoon() {
        return "deadline".equals(sortBy);
    }

    /**
     * 지역 필터 활성 여부 확인
     */
    public boolean hasRegionFilter() {
        return region != null && !"전체".equals(region) && !region.trim().isEmpty();
    }

    /**
     * 날짜 필터 활성 여부 확인
     */
    public boolean hasDateFilter() {
        return startDate != null || endDate != null;
    }
}
