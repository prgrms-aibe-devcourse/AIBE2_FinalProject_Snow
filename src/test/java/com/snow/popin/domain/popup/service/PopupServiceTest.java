package com.snow.popin.domain.popup.service;

import com.snow.popin.domain.popup.dto.request.PopupListRequestDto;
import com.snow.popin.domain.popup.dto.response.PopupDetailResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupListResponseDto;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.global.exception.PopupNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PopupServiceTest {

    @Mock
    private PopupRepository popupRepository;

    @InjectMocks
    private PopupService popupService;

    @Test
    @DisplayName("팝업 리스트 조회 - 정상 케이스")
    void getPopupList_정상조회_테스트() {
        // given
        PopupListRequestDto request = createListRequest();
        request.setStatus(PopupStatus.ONGOING);

        List<Popup> popups = Arrays.asList(
                createMockPopupForSummary(1L, "팝업1", PopupStatus.ONGOING),
                createMockPopupForSummary(2L, "팝업2", PopupStatus.ONGOING)
        );
        Page<Popup> pageResult = new PageImpl<>(popups);

        when(popupRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getPopupList(request);

        // then
        assertThat(result.getPopups()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(2);

        verify(popupRepository).findWithFilters(
                eq(PopupStatus.ONGOING),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 마감임박 필터")
    void getPopupList_마감임박필터_테스트() {
        // given
        PopupListRequestDto request = createListRequest();
        request.setSortBy("deadline");

        List<Popup> popups = Arrays.asList(createMockPopupForSummary(1L, "마감임박 팝업", PopupStatus.ONGOING));
        Page<Popup> pageResult = new PageImpl<>(popups);

        when(popupRepository.findDeadlineSoonPopups(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getPopupList(request);

        // then
        assertThat(result.getPopups()).hasSize(1);
        verify(popupRepository).findDeadlineSoonPopups(any(LocalDate.class), any(Pageable.class));
        verify(popupRepository, never()).findWithFilters(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 지역 필터")
    void getPopupList_지역필터_테스트() {
        // given
        PopupListRequestDto request = createListRequest();
        request.setRegion("강남구");

        List<Popup> popups = Arrays.asList(createMockPopupForSummary(1L, "강남 팝업", PopupStatus.ONGOING));
        Page<Popup> pageResult = new PageImpl<>(popups);

        when(popupRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getPopupList(request);

        // then
        assertThat(result.getPopups()).hasSize(1);
        verify(popupRepository).findWithFilters(
                isNull(),
                eq("강남구"),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 빈 결과")
    void getPopupList_빈결과_테스트() {
        // given
        PopupListRequestDto request = createListRequest();
        Page<Popup> emptyPage = new PageImpl<>(Collections.emptyList());

        when(popupRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        // when
        PopupListResponseDto result = popupService.getPopupList(request);

        // then
        assertThat(result.getPopups()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("팝업 상세 조회 - 정상 케이스")
    void getPopupDetail_정상조회_테스트() {
        // given
        Long popupId = 1L;
        Popup popup = createMockPopupForDetail(popupId, "상세 팝업", PopupStatus.ONGOING);

        when(popupRepository.findByIdWithDetails(popupId))
                .thenReturn(Optional.of(popup));

        // when
        PopupDetailResponseDto result = popupService.getPopupDetail(popupId);

        // then
        assertThat(result.getId()).isEqualTo(popupId);
        assertThat(result.getTitle()).isEqualTo("상세 팝업");
        assertThat(result.getStatus()).isEqualTo(PopupStatus.ONGOING);

        verify(popupRepository).findByIdWithDetails(popupId);
    }

    @Test
    @DisplayName("팝업 상세 조회 - 존재하지 않는 ID")
    void getPopupDetail_존재하지않는ID_예외발생() {
        // given
        Long invalidId = 999L;
        when(popupRepository.findByIdWithDetails(invalidId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> popupService.getPopupDetail(invalidId))
                .isInstanceOf(PopupNotFoundException.class);

        verify(popupRepository).findByIdWithDetails(invalidId);
    }

    @Test
    @DisplayName("추천 팝업 조회 - 정상 케이스")
    void getFeaturedPopups_정상조회_테스트() {
        // given
        int page = 0;
        int size = 10;

        List<Popup> featuredPopups = Arrays.asList(
                createMockPopupForSummary(1L, "추천 팝업1", PopupStatus.ONGOING),
                createMockPopupForSummary(2L, "추천 팝업2", PopupStatus.PLANNED)
        );
        Page<Popup> pageResult = new PageImpl<>(featuredPopups);

        when(popupRepository.findPopularPopups(any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getPopularPopups(page, size);

        // then
        assertThat(result.getPopups()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(popupRepository).findPopularPopups(any(Pageable.class));
    }

    @Test
    @DisplayName("페이지 크기 검증 - 최대값 제한")
    void getPopupList_페이지크기제한_테스트() {
        // given
        PopupListRequestDto request = createListRequest();
        request.setSize(200); // 최대값 100을 초과

        Page<Popup> pageResult = new PageImpl<>(Collections.emptyList());
        when(popupRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(pageResult);

        // when
        popupService.getPopupList(request);

        // then - size가 100으로 제한되었는지 확인 (Pageable 검증)
        verify(popupRepository).findWithFilters(any(), any(), any(), any(), argThat(pageable ->
                pageable.getPageSize() == 100
        ));
    }

    @Test
    @DisplayName("페이지 번호 검증 - 음수 처리")
    void getPopupList_음수페이지_테스트() {
        // given
        PopupListRequestDto request = createListRequest();
        request.setPage(-1); // 음수 페이지

        Page<Popup> pageResult = new PageImpl<>(Collections.emptyList());
        when(popupRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(pageResult);

        // when
        popupService.getPopupList(request);

        // then - page가 0으로 조정되었는지 확인
        verify(popupRepository).findWithFilters(any(), any(), any(), any(), argThat(pageable ->
                pageable.getPageNumber() == 0
        ));
    }

    private PopupListRequestDto createListRequest() {
        PopupListRequestDto request = new PopupListRequestDto();
        request.setPage(0);
        request.setSize(20);
        return request;
    }

    private Popup createMockPopupForSummary(Long id, String title, PopupStatus status) {
        Popup popup = mock(Popup.class);

        when(popup.getId()).thenReturn(id);
        when(popup.getTitle()).thenReturn(title);
        when(popup.getSummary()).thenReturn("테스트 요약");
        when(popup.getPeriodText()).thenReturn("2024.01.01 - 2024.01.08");
        when(popup.getStatus()).thenReturn(status);
        when(popup.getMainImageUrl()).thenReturn("test-image.jpg");
        when(popup.getIsFeatured()).thenReturn(false);
        when(popup.getReservationAvailable()).thenReturn(false);
        when(popup.getWaitlistAvailable()).thenReturn(false);
        when(popup.getEntryFee()).thenReturn(0);
        when(popup.isFreeEntry()).thenReturn(true);
        when(popup.getFeeDisplayText()).thenReturn("무료");
        when(popup.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(popup.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(popup.getImages()).thenReturn(Collections.emptySet());
        when(popup.getVenueName()).thenReturn("테스트 장소");
        when(popup.getVenueAddress()).thenReturn("테스트 주소");
        when(popup.getRegion()).thenReturn("강남구");
        when(popup.getParkingAvailable()).thenReturn(false);

        return popup;
    }

    private Popup createMockPopupForDetail(Long id, String title, PopupStatus status) {
        Popup popup = mock(Popup.class);

        when(popup.getId()).thenReturn(id);
        when(popup.getTitle()).thenReturn(title);
        when(popup.getSummary()).thenReturn("테스트 요약");
        when(popup.getStartDate()).thenReturn(LocalDate.now());
        when(popup.getEndDate()).thenReturn(LocalDate.now().plusDays(7));
        when(popup.getPeriodText()).thenReturn("2024.01.01 - 2024.01.08");
        when(popup.getStatus()).thenReturn(status);
        when(popup.getMainImageUrl()).thenReturn("test-image.jpg");
        when(popup.getIsFeatured()).thenReturn(false);
        when(popup.getReservationAvailable()).thenReturn(false);
        when(popup.getWaitlistAvailable()).thenReturn(false);
        when(popup.getEntryFee()).thenReturn(0);
        when(popup.isFreeEntry()).thenReturn(true);
        when(popup.getFeeDisplayText()).thenReturn("무료");
        when(popup.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(popup.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(popup.getVenueName()).thenReturn("테스트 장소");
        when(popup.getVenueAddress()).thenReturn("테스트 주소");
        when(popup.getRegion()).thenReturn("강남구");
        when(popup.getParkingAvailable()).thenReturn(false);
        when(popup.getImages()).thenReturn(Collections.emptySet());
        when(popup.getHours()).thenReturn(Collections.emptySet());

        return popup;
    }
}