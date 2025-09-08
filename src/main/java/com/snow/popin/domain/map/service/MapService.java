package com.snow.popin.domain.map.service;

import com.snow.popin.domain.map.repository.MapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MapService {

    private final MapRepository mapRepository;

    // 모든 지역 목록 조회 (검색 필터용)
    public List<String> getAllRegions() {
        log.info("지역 목록 조회 완료");

        //TODO: 지역 표시

        return List.of("서울", "인천", "부산", "대구", "대전", "광주", "울산");
    }
}
