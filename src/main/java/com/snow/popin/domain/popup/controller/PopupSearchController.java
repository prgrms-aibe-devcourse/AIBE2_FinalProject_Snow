package com.snow.popin.domain.popup.controller;

import com.snow.popin.domain.popup.dto.request.PopupSearchRequestDto;
import com.snow.popin.domain.popup.dto.response.PopupListResponseDto;
import com.snow.popin.domain.popup.service.PopupSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class PopupSearchController {

    private final PopupSearchService popupSearchService;

    //  팝업스토어 검색 (제목, 태그)
    @GetMapping("/popups")
    public ResponseEntity<PopupListResponseDto> searchPopups(@Valid PopupSearchRequestDto request) {
        PopupListResponseDto response = popupSearchService.searchPopups(request);
        return ResponseEntity.ok(response);
    }
}
