package com.snow.popin.domain.popup.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Controller
public class PopupPageController {

    // 팝업 상세 페이지
    @GetMapping("/popup/{id}")
    public String popupDetail(@PathVariable Long id) {
        log.info("팝업 상세 페이지 요청 - ID: {}", id);
        // 정적 리소스 경로로 forward (redirect 대신)
        return "forward:/templates/pages/popup/popup-detail.html";
    }

    // 팝업 검색 페이지
    @GetMapping("/popup/search")
    public String popupSearch() {
        log.info("팝업 검색 페이지 요청");
        return "forward:/templates/pages/popup/popup-search.html";
    }

    // 지도 페이지
    @GetMapping("/map")
    public String mapPage() {
        log.info("지도 페이지 요청");
        return "forward:/templates/pages/popup/map.html";
    }
}