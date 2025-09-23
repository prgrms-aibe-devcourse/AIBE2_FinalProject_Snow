package com.snow.popin.domain.popup.service;

import com.snow.popin.domain.popup.dto.request.PopupSearchRequestDto;
import com.snow.popin.domain.popup.dto.response.*;
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

import java.math.BigInteger;
import java.util.*;
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

        // 검색어 길이 체크 (최소 2글자)
        if (query == null || query.length() < 2) {
            log.info("검색어 최소 길이 미충족 - 검색어: '{}', 길이: {}",
                    request.getQuery(), query != null ? query.length() : 0);
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

    // 자동완성 리스트 조회
    // 팝업 제목과 태그에서 검색어와 일치하는 항목들을 찾아서 반환
    public AutocompleteResponseDto getAutocompleteSuggestions(String query) {
        log.info("자동완성 제안 조회 - 검색어: {}", query);

        // 검색어 전처리
        String preprocessedQuery = preprocessQuery(query);

        // 검색어가 너무 짧으면 빈 결과 반환
        if (preprocessedQuery == null || preprocessedQuery.length() < 1) {
            log.info("자동완성 검색어 길이 부족 - 검색어: {}", query);
            return AutocompleteResponseDto.empty(query);
        }

        try {
            // 통합 검색어 제안 조회 (최대 10개)
            List<Object[]> rawSuggestions = popupSearchRepository.findSuggestions(preprocessedQuery, 10);

            List<AutocompleteSuggestionDto> suggestions = rawSuggestions.stream()
                    .map(this::convertToSuggestionDto)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("자동완성 제안 완료 - 검색어: {}, 결과 수: {}", query, suggestions.size());
            return AutocompleteResponseDto.of(suggestions, query);

        } catch (Exception e) {
            log.error("자동완성 제안 조회 실패 - 검색어: {}", query, e);
            return AutocompleteResponseDto.empty(query);
        }
    }

    // 간단한 자동완성 조회
    public AutocompleteResponseDto getSimpleAutocompleteSuggestions(String query) {
        log.info("간단한 자동완성 제안 조회 - 검색어: {}", query);

        String preprocessedQuery = preprocessQuery(query);

        if (preprocessedQuery == null || preprocessedQuery.length() < 1) {
            return AutocompleteResponseDto.empty(query);
        }

        try {
            Pageable limitPageable = PageRequest.of(0, 10); // 최대 10개씩

            // 팝업 제목에서 자동완성 리스트 가져오기
            List<String> titleSuggestions = popupSearchRepository.findPopupTitleSuggestions(preprocessedQuery, limitPageable);
            log.info("제목 제안 결과 - 검색어: {}, 결과: {}", preprocessedQuery, titleSuggestions);

            // 태그에서 자동완성 리스트 가져오기
            List<String> tagSuggestions = popupSearchRepository.findTagSuggestions(preprocessedQuery, limitPageable);
            log.info("태그 제안 결과 - 검색어: {}, 결과: {}", preprocessedQuery, tagSuggestions);

            // 자동완성 리스트 조합 및 중복 제거
            Set<String> allSuggestions = new LinkedHashSet<>();
            allSuggestions.addAll(titleSuggestions);
            allSuggestions.addAll(tagSuggestions);

            log.info("통합 제안 결과 - 검색어: {}, 중복 제거 후: {}", preprocessedQuery, allSuggestions);

            List<AutocompleteSuggestionDto> suggestions = allSuggestions.stream()
                    .limit(8) // 최대 8개
                    .map(suggestion -> {
                        String type = titleSuggestions.contains(suggestion) ? "title" : "tag";
                        return AutocompleteSuggestionDto.of(suggestion, type, 0L);
                    })
                    .collect(Collectors.toList());

            log.info("간단한 자동완성 제안 완료 - 검색어: {}, 결과 수: {}", query, suggestions.size());
            if (!suggestions.isEmpty()) {
                log.info("반환될 제안들: {}", suggestions.stream()
                        .map(s -> s.getText() + "(" + s.getType() + ")")
                        .collect(Collectors.toList()));
            }

            return AutocompleteResponseDto.of(suggestions, query);

        } catch (Exception e) {
            log.error("간단한 자동완성 제안 조회 실패 - 검색어: {}", query, e);
            return AutocompleteResponseDto.empty(query);
        }
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

    private AutocompleteSuggestionDto convertToSuggestionDto(Object[] row) {
        try {
            String suggestion = (String) row[0];
            String type = (String) row[1];

            // popularity는 BigInteger 또는 Long일 수 있음
            Long popularity = 0L;
            if (row[2] != null) {
                if (row[2] instanceof BigInteger) {
                    popularity = ((BigInteger) row[2]).longValue();
                } else if (row[2] instanceof Long) {
                    popularity = (Long) row[2];
                } else if (row[2] instanceof Integer) {
                    popularity = ((Integer) row[2]).longValue();
                }
            }

            return AutocompleteSuggestionDto.of(suggestion, type, popularity);
        } catch (Exception e) {
            log.warn("자동완성 제안 변환 실패: {}", Arrays.toString(row), e);
            return null;
        }
    }
}