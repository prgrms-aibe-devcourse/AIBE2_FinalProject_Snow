package com.snow.popin.domain.popup.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PopupImageResponseDto {
    private Long id;
    private String imageUrl;
    private String caption;
    private Integer sortOrder;
}