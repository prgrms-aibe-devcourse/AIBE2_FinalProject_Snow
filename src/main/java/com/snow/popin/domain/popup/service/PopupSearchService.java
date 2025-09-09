package com.snow.popin.domain.popup.service;

import com.snow.popin.domain.popup.dto.request.PopupSearchRequestDto;
import com.snow.popin.domain.popup.dto.response.PopupImageResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupListResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupSummaryResponseDto;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupImage;
import com.snow.popin.domain.popup.repository.PopupSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PopupSearchService {

    private final PopupSearchRepository popupSearchRepository;

    public PopupListResponseDto searchPopups(PopupSearchRequestDto request) {
        log.info("팝업 검색 시작 - 검색어: {}, 페이지: {}, 크기: {}",
                request.getQuery(), request.getPage(), request.getSize());

        Pageable pageable = createSearchPageable(request.getPage(), request.getSize());
        String query = preprocessQuery(request.getQuery());

        if (query == null || query.length() < 2) {
           log.info("검색어 최소 길이 미충족 - 검색어: {}", request.getQuery());
            return PopupListResponseDto.of(new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0), List.of());
        }

        Page<Popup> popupPage = popupSearchRepository.searchByTitleAndTags(query, pageable);

        List<PopupSummaryResponseDto> popupDtos = popupPage.getContent()
                .stream()
                .map(PopupSummaryResponseDto::from)
                .collect(Collectors.toList());

        log.info("팝업 검색 완료 - 검색어: {}, 결과 수: {}", query, popupPage.getTotalElements());

        return PopupListResponseDto.of(popupPage, popupDtos);
    }

    private String preprocessQuery(String query) {
        if (query == null) {
            return null;
        }

        return query.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    private Pageable createSearchPageable(int page, int size) {
        int validPage = Math.max(0, page);
        int validSize = Math.min(Math.max(1, size), 100);
        return PageRequest.of(validPage, validSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}