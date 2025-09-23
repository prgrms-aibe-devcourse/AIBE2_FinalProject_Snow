package com.snow.popin.domain.popup.controller;

import com.snow.popin.domain.popup.dto.request.PopupSearchRequestDto;
import com.snow.popin.domain.popup.dto.response.AutocompleteResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupListResponseDto;
import com.snow.popin.domain.popup.service.PopupSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class PopupSearchController {

    private final PopupSearchService popupSearchService;

    // 팝업스토어 검색 (제목, 태그)
    @GetMapping("/popups")
    public ResponseEntity<PopupListResponseDto> searchPopups(@Valid PopupSearchRequestDto request) {
        log.info("팝업 검색 요청 - query: {}, page: {}, size: {}",
                request.getQuery(), request.getPage(), request.getSize());

        PopupListResponseDto response = popupSearchService.searchPopups(request);
        return ResponseEntity.ok(response);
    }

    // 자동완성 리스트 조회
    // 팝업 제목과 태그에서 검색어와 일치하는 항목들을 반환
    @GetMapping("/suggestions")
    public ResponseEntity<AutocompleteResponseDto> getAutocompleteSuggestions(
            @RequestParam(value = "q", required = false) String query) {

        log.info("자동완성 제안 요청 - query: {}", query);

        // 검색어가 없거나 너무 짧으면 빈 결과 반환
        if (query == null || query.trim().length() < 1) {
            log.info("자동완성 검색어 부족 - query: {}", query);
            return ResponseEntity.ok(AutocompleteResponseDto.empty(query));
        }

        try {
            // 간단한 검색만 사용 (안정적이고 모든 상태 포함)
            AutocompleteResponseDto response = popupSearchService.getSimpleAutocompleteSuggestions(query);

            log.info("자동완성 제안 완료 - query: {}, 결과 수: {}", query, response.getTotalCount());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("자동완성 제안 요청 실패 - query: {}", query, e);
            return ResponseEntity.ok(AutocompleteResponseDto.empty(query));
        }
    }
}