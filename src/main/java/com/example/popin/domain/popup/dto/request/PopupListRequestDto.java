package com.example.popin.domain.popup.dto.request;

import com.example.popin.domain.popup.entity.PopupStatus;
import lombok.Data;

@Data
public class PopupListRequestDto {
    private PopupStatus status;
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
