package com.snow.popin.domain.popup.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PopupListResponseDto {
    private List<PopupSummaryResponseDto> popups;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
}
