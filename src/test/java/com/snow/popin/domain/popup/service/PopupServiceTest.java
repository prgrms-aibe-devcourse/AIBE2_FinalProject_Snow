package com.snow.popin.domain.popup.service;

import com.snow.popin.domain.popup.dto.response.PopupDetailResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupListResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupSummaryResponseDto;
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

    // 메인 페이지 필터링 테스트
    @Test
    @DisplayName("전체 팝업 조회 - 상태별 필터링")
    void getAllPopups_상태필터_테스트() {
        // given
        PopupStatus status = PopupStatus.ONGOING;
        List<Popup> popups = Arrays.asList(
                createMockPopupForSummary(1L, "진행중 팝업1", PopupStatus.ONGOING),
                createMockPopupForSummary(2L, "진행중 팝업2", PopupStatus.ONGOING)
        );
        Page<Popup> pageResult = new PageImpl<>(popups);

        when(popupRepository.findAllWithStatusFilter(eq(status), any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getAllPopups(0, 20, status);

        // then
        assertThat(result.getPopups()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(popupRepository).findAllWithStatusFilter(eq(status), any(Pageable.class));
    }

    @Test
    @DisplayName("인기 팝업 조회 - 조회수 기준")
    void getPopularPopups_조회수기준_테스트() {
        // given
        PopupStatus status = PopupStatus.ONGOING;
        List<Popup> popups = Arrays.asList(
                createMockPopupWithViewCount(1L, "인기 팝업1", 1000L),
                createMockPopupWithViewCount(2L, "인기 팝업2", 500L)
        );
        Page<Popup> pageResult = new PageImpl<>(popups);

        when(popupRepository.findPopularActivePopups(any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getPopularPopups(0, 20);

        // then
        assertThat(result.getPopups()).hasSize(2);
        verify(popupRepository).findPopularActivePopups(any(Pageable.class));
    }

    @Test
    @DisplayName("마감임박 팝업 조회")
    void getDeadlineSoonPopups_테스트() {
        // given
        PopupStatus status = PopupStatus.ONGOING;
        List<Popup> popups = Arrays.asList(
                createMockPopupForSummary(1L, "마감임박 팝업", PopupStatus.ONGOING)
        );
        Page<Popup> pageResult = new PageImpl<>(popups);

        when(popupRepository.findDeadlineSoonPopups(eq(status), any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getDeadlineSoonPopups(0, 20, status);

        // then
        assertThat(result.getPopups()).hasSize(1);
        verify(popupRepository).findDeadlineSoonPopups(eq(status), any(Pageable.class));
    }

    @Test
    @DisplayName("지역별 + 날짜별 팝업 조회 - 7일 필터")
    void getPopupsByRegionAndDate_7일필터_테스트() {
        // given
        String region = "강남구";
        PopupStatus status = PopupStatus.ONGOING;
        String dateFilter = "7days";

        List<Popup> popups = Arrays.asList(
                createMockPopupForSummary(1L, "강남 팝업", PopupStatus.ONGOING)
        );
        Page<Popup> pageResult = new PageImpl<>(popups);

        when(popupRepository.findByRegionAndDateRange(
                eq(region), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getPopupsByRegionAndDate(
                region, status, dateFilter, null, null, 0, 20);

        // then
        assertThat(result.getPopups()).hasSize(1);
        verify(popupRepository).findByRegionAndDateRange(
                eq(region), any(LocalDate.class), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    @DisplayName("지역별 + 날짜별 팝업 조회 - 사용자 지정 기간")
    void getPopupsByRegionAndDate_사용자지정기간_테스트() {
        // given
        String region = "종로구";
        PopupStatus status = null;
        String dateFilter = "custom";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);

        List<Popup> popups = Arrays.asList(
                createMockPopupForSummary(1L, "종로 팝업", PopupStatus.PLANNED)
        );
        Page<Popup> pageResult = new PageImpl<>(popups);

        when(popupRepository.findByRegionAndDateRange(
                eq(region), eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getPopupsByRegionAndDate(
                region, status, dateFilter, startDate, endDate, 0, 20);

        // then
        assertThat(result.getPopups()).hasSize(1);
        verify(popupRepository).findByRegionAndDateRange(
                eq(region),eq(startDate), eq(endDate), any(Pageable.class));
    }

    @Test
    @DisplayName("AI 추천 팝업 조회 - 현재는 인기 팝업으로 대체")
    void getAIRecommendedPopups_테스트() {
        // given
        String token = "test-token";
        List<Popup> popups = Arrays.asList(
                createMockPopupWithViewCount(1L, "AI 추천 팝업", 2000L)
        );
        Page<Popup> pageResult = new PageImpl<>(popups);

        when(popupRepository.findPopularActivePopups(any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getAIRecommendedPopups(token, 0, 20);

        // then
        assertThat(result.getPopups()).hasSize(1);
        verify(popupRepository).findPopularActivePopups(any(Pageable.class));
    }

    // 팝업 상세 조회 테스트
    @Test
    @DisplayName("팝업 상세 조회 - 조회수 증가")
    void getPopupDetail_조회수증가_테스트() {
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
        verify(popup).incrementViewCount(); // 조회수 증가 확인
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
    }

    // 추천 및 유사 팝업 테스트
    @Test
    @DisplayName("유사한 팝업 조회")
    void getSimilarPopups_테스트() {
        // given
        String categoryName = "패션";
        Long excludeId = 1L;
        List<Popup> similarPopups = Arrays.asList(
                createMockPopupForSummary(2L, "유사 팝업1", PopupStatus.ONGOING),
                createMockPopupForSummary(3L, "유사 팝업2", PopupStatus.PLANNED)
        );
        Page<Popup> pageResult = new PageImpl<>(similarPopups);

        when(popupRepository.findSimilarPopups(eq(categoryName), eq(excludeId), any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getSimilarPopups(categoryName, excludeId, 0, 4);

        // then
        assertThat(result.getPopups()).hasSize(2);
        verify(popupRepository).findSimilarPopups(eq(categoryName), eq(excludeId), any(Pageable.class));
    }

    @Test
    @DisplayName("카테고리별 추천 팝업 조회")
    void getRecommendedPopupsBySelectedCategories_테스트() {
        // given
        List<Long> categoryIds = Arrays.asList(1L, 2L);
        List<Popup> recommendedPopups = Arrays.asList(
                createMockPopupWithViewCount(1L, "추천 팝업1", 1500L),
                createMockPopupWithViewCount(2L, "추천 팝업2", 1200L)
        );
        Page<Popup> pageResult = new PageImpl<>(recommendedPopups);

        when(popupRepository.findRecommendedPopupsByCategories(eq(categoryIds), any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getRecommendedPopupsBySelectedCategories(categoryIds, 0, 20);

        // then
        assertThat(result.getPopups()).hasSize(2);
        verify(popupRepository).findRecommendedPopupsByCategories(eq(categoryIds), any(Pageable.class));
    }

    // 카테고리 및 지역별 조회 테스트
    @Test
    @DisplayName("카테고리별 팝업 조회")
    void getPopupsByCategory_테스트() {
        // given
        String categoryName = "뷰티";
        List<Popup> categoryPopups = Arrays.asList(
                createMockPopupForSummary(1L, "뷰티 팝업1", PopupStatus.ONGOING)
        );
        Page<Popup> pageResult = new PageImpl<>(categoryPopups);

        when(popupRepository.findByCategoryName(eq(categoryName), any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        PopupListResponseDto result = popupService.getPopupsByCategory(categoryName, 0, 20);

        // then
        assertThat(result.getPopups()).hasSize(1);
        verify(popupRepository).findByCategoryName(eq(categoryName), any(Pageable.class));
    }

    @Test
    @DisplayName("지역별 팝업 조회")
    void getPopupsByRegion_테스트() {
        // given
        String region = "홍대";
        List<Popup> regionPopups = Arrays.asList(
                createMockPopupForSummary(1L, "홍대 팝업1", PopupStatus.ONGOING),
                createMockPopupForSummary(2L, "홍대 팝업2", PopupStatus.PLANNED)
        );

        when(popupRepository.findByRegion(eq(region)))
                .thenReturn(regionPopups);

        // when
        List<PopupSummaryResponseDto> result = popupService.getPopupsByRegion(region);

        // then
        assertThat(result).hasSize(2);
        verify(popupRepository).findByRegion(eq(region));
    }

    // 유틸리티 테스트
    @Test
    @DisplayName("상태 파싱 테스트")
    void parseStatus_테스트() {
        // given & when & then
        assertThat(popupService.parseStatus("ONGOING")).isEqualTo(PopupStatus.ONGOING);
        assertThat(popupService.parseStatus("PLANNED")).isEqualTo(PopupStatus.PLANNED);
        assertThat(popupService.parseStatus("ENDED")).isEqualTo(PopupStatus.ENDED);
        assertThat(popupService.parseStatus("전체")).isNull();
        assertThat(popupService.parseStatus(null)).isNull();
        assertThat(popupService.parseStatus("")).isNull();
        assertThat(popupService.parseStatus("INVALID")).isNull();
    }

    @Test
    @DisplayName("페이지 크기 검증 - 최대값 제한")
    void getAllPopups_페이지크기제한_테스트() {
        // given
        Page<Popup> pageResult = new PageImpl<>(Collections.emptyList());
        when(popupRepository.findAllWithStatusFilter(any(), any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        popupService.getAllPopups(0, 200, null); // 최대값 100 초과

        // then - size가 100으로 제한되었는지 확인
        verify(popupRepository).findAllWithStatusFilter(any(), argThat(pageable ->
                pageable.getPageSize() == 100
        ));
    }

    @Test
    @DisplayName("페이지 번호 검증 - 음수 처리")
    void getAllPopups_음수페이지_테스트() {
        // given
        Page<Popup> pageResult = new PageImpl<>(Collections.emptyList());
        when(popupRepository.findAllWithStatusFilter(any(), any(Pageable.class)))
                .thenReturn(pageResult);

        // when
        popupService.getAllPopups(-1, 20, null); // 음수 페이지

        // then - page가 0으로 조정되었는지 확인
        verify(popupRepository).findAllWithStatusFilter(any(), argThat(pageable ->
                pageable.getPageNumber() == 0
        ));
    }

    // Helper Methods
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
        when(popup.getViewCount()).thenReturn(100L); // 조회수 추가
        when(popup.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(popup.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(popup.getImages()).thenReturn(Collections.emptySet());
        when(popup.getVenueName()).thenReturn("테스트 장소");
        when(popup.getVenueAddress()).thenReturn("테스트 주소");
        when(popup.getRegion()).thenReturn("강남구");
        when(popup.getParkingAvailable()).thenReturn(false);

        return popup;
    }

    private Popup createMockPopupWithViewCount(Long id, String title, Long viewCount) {
        Popup popup = createMockPopupForSummary(id, title, PopupStatus.ONGOING);
        when(popup.getViewCount()).thenReturn(viewCount);
        return popup;
    }

    private Popup createMockPopupForDetail(Long id, String title, PopupStatus status) {
        Popup popup = mock(Popup.class);

        when(popup.getId()).thenReturn(id);
        when(popup.getTitle()).thenReturn(title);
        when(popup.getSummary()).thenReturn("테스트 상세 요약");
        when(popup.getStartDate()).thenReturn(LocalDate.now());
        when(popup.getEndDate()).thenReturn(LocalDate.now().plusDays(7));
        when(popup.getPeriodText()).thenReturn("2024.01.01 - 2024.01.08");
        when(popup.getStatus()).thenReturn(status);
        when(popup.getMainImageUrl()).thenReturn("test-detail-image.jpg");
        when(popup.getIsFeatured()).thenReturn(false);
        when(popup.getReservationAvailable()).thenReturn(true);
        when(popup.getWaitlistAvailable()).thenReturn(false);
        when(popup.getEntryFee()).thenReturn(5000);
        when(popup.isFreeEntry()).thenReturn(false);
        when(popup.getFeeDisplayText()).thenReturn("5,000원");
        when(popup.getViewCount()).thenReturn(250L); // 조회수 추가
        when(popup.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(popup.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(popup.getVenueName()).thenReturn("테스트 상세 장소");
        when(popup.getVenueAddress()).thenReturn("테스트 상세 주소");
        when(popup.getRegion()).thenReturn("강남구");
        when(popup.getParkingAvailable()).thenReturn(true);
        when(popup.getImages()).thenReturn(Collections.emptySet());
        when(popup.getHours()).thenReturn(Collections.emptySet());
        when(popup.updateStatus()).thenReturn(false); // 상태 업데이트 불필요

        return popup;
    }
}