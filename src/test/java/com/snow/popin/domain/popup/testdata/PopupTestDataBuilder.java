package com.snow.popin.domain.popup.testdata;

import com.snow.popin.domain.map.entity.Venue;
import com.snow.popin.domain.popup.dto.request.PopupListRequestDto;
import com.snow.popin.domain.popup.entity.*;

import java.time.LocalDate;

public class PopupTestDataBuilder {

    // 기본 팝업 생성
    public static Popup createPopup(String title, PopupStatus status, Venue venue) {
        return Popup.createForTest(title, status, venue);
    }

    // 날짜 지정 팝업 생성
    public static Popup createPopupWithDates(String title, LocalDate startDate, LocalDate endDate, Venue venue) {
        return Popup.createForTestWithDates(title, startDate, endDate, venue);
    }

    // 추천 팝업 생성
    public static Popup createFeaturedPopup(String title, PopupStatus status, Venue venue) {
        return Popup.createFeaturedForTest(title, status, venue);
    }

    // 기본 장소 생성
    public static Venue createVenue(String region) {
        return Venue.createForTest(region);
    }

    // 주차 가능 장소 생성
    public static Venue createVenueWithParking(String region, boolean parkingAvailable) {
        return Venue.createForTestWithParking(region, parkingAvailable);
    }

    // 기본 요청 DTO 생성
    public static PopupListRequestDto createListRequest() {
        PopupListRequestDto request = new PopupListRequestDto();
        request.setPage(0);
        request.setSize(20);
        return request;
    }

    // 상태 필터 요청 DTO 생성
    public static PopupListRequestDto createListRequestWithStatus(PopupStatus status) {
        PopupListRequestDto request = createListRequest();
        request.setStatus(status);
        return request;
    }

    // 지역 필터 요청 DTO 생성
    public static PopupListRequestDto createListRequestWithRegion(String region) {
        PopupListRequestDto request = createListRequest();
        request.setRegion(region);
        return request;
    }

    // 정렬 요청 DTO 생성
    public static PopupListRequestDto createListRequestWithSort(String sortBy) {
        PopupListRequestDto request = createListRequest();
        request.setSortBy(sortBy);
        return request;
    }
}