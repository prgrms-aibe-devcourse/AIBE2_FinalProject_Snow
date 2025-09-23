package com.snow.popin.domain.popup.dto.request;

import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Getter
public class PopupSearchRequestDto {

    private String query;

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 20;

    public void setQuery(String query) {
        this.query = query;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean hasQuery() {
        return query != null && !query.trim().isEmpty();
    }

    public boolean isValidQueryLength() {
        return hasQuery() && query.trim().length() >= 2;
    }
}