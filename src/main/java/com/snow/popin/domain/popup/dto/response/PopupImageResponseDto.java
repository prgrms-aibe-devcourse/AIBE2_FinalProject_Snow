package com.snow.popin.domain.popup.dto.response;

import lombok.Builder;

@Builder
public class PopupImageResponseDto {
    private Long id;
    private String imageUrl;
    private String caption;
    private Integer sortOrder;
}