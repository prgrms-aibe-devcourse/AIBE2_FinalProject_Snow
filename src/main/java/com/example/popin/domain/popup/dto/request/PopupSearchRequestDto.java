package com.example.popin.domain.popup.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

@Data
public class PopupSearchRequestDto {
    private String title;
    private List<String> tags;
    private String region;

    // 페이징
    @Min(0)
    private int page = 0;

    @Min(1)
    private int size = 20;

    // 정렬
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}