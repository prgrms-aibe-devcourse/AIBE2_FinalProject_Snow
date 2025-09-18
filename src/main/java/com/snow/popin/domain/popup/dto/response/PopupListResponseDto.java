package com.snow.popin.domain.popup.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
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

    // 빈 응답을 생성
    public static PopupListResponseDto empty() {
        return empty(0, 20); // 기본 페이지 크기
    }

    // 페이지 정보와 함께 빈 응답을 생성
    public static PopupListResponseDto empty(int page, int size) {
        Page<PopupSummaryResponseDto> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(page, size),
                0
        );

        return new PopupListResponseDto(
                Collections.emptyList(),
                0,  // totalPages
                0,  // totalElements
                page,
                size,
                false,  // hasNext
                false   // hasPrevious
        );
    }
}
