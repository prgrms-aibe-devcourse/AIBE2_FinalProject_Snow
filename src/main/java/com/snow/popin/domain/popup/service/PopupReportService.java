// src/main/java/com/snow/popin/domain/popup/service/PopupReportService.java
package com.snow.popin.domain.popup.service;

import com.snow.popin.domain.popup.entity.PopupReport;
import com.snow.popin.domain.popup.repository.PopupReportRepository;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.domain.user.service.UserService;
import com.snow.popin.global.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupReportService {

    private final PopupReportRepository repository;
    private final UserService userService;
    private final UserUtil userUtil;

    @Transactional
    public PopupReport create(String brandName,
                              String popupName,
                              String address,
                              LocalDate startDate,
                              LocalDate endDate,
                              String extraInfo,
                              List<String> images) {
        Long userId = userUtil.getCurrentUserId();
        User reporter = userService.findById(userId);
        return repository.save(
                PopupReport.builder()
                        .reporter(reporter)
                        .brandName(brandName)
                        .popupName(popupName)
                        .address(address)
                        .startDate(startDate)
                        .endDate(endDate)
                        .extraInfo(extraInfo)
                        .images(images)
                        .build()
        );
    }

    public PopupReport getOne(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("제보가 존재하지 않습니다: " + id));
    }

    public Page<PopupReport> listByStatus(PopupReport.Status status, Pageable pageable) {
        return repository.findAllByStatusOrderByCreatedAtDesc(status, pageable);
    }

    public Page<PopupReport> listMine(Pageable pageable) {
        Long userId = userUtil.getCurrentUserId();
        User me = userService.findById(userId);
        return repository.findAllByReporterOrderByCreatedAtDesc(me, pageable);
    }

    @Transactional
    public void approve(Long id) {
        PopupReport r = getOne(id);
        r.approve();
    }

    @Transactional
    public void reject(Long id) {
        PopupReport r = getOne(id);
        r.reject();
    }
}
