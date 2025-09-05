package com.snow.popin.domain.popup.dto.request;

import com.snow.popin.domain.popup.entity.PopupStatus;
import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class PopupListRequestDto {
    private PopupStatus status;

    @Min(0)
    private int page = 0;

    @Min(1)
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
