package com.snow.popin.domain.popup.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

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

    public static PopupListResponseDto of(Page<?> page, List<PopupSummaryResponseDto> content) {
        return PopupListResponseDto.builder()
                .popups(content)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
