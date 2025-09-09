package com.snow.popin.domain.map.controller;

import com.snow.popin.domain.map.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    // 모든 지역 목록 조회 (필터용)
    @GetMapping("/regions")
    public ResponseEntity<List<String>> getAllRegions() {
        List<String> regions = mapService.getAllRegions();
        return ResponseEntity.ok(regions);
    }
}