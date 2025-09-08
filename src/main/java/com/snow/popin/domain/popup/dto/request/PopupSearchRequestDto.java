package com.snow.popin.domain.popup.dto.request;

import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.Min;
import java.util.List;

@Getter
public class PopupSearchRequestDto {
    // 검색어 (제목, 태그에서 검색)
    private String query;

    // 페이징
    @Min(0)
    private int page = 0;

    @Min(1)
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

    // 검색어 유효성 확인
    public boolean hasQuery() {
        return query != null && !query.trim().isEmpty();
    }

    // 검색어 길이 확인
    public boolean isValidQueryLength() {
        return hasQuery() && query.trim().length() >= 2;
    }
}