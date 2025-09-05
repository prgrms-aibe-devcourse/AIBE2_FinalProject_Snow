package com.snow.popin.domain.popup.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
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
