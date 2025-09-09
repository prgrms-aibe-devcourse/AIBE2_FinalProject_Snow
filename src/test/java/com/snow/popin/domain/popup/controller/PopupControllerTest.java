package com.snow.popin.domain.popup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snow.popin.domain.popup.dto.response.PopupDetailResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupListResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupSummaryResponseDto;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.service.PopupService;
import com.snow.popin.global.exception.PopupNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PopupController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class  // 이 부분이 핵심!
)
class PopupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PopupService popupService;

    @MockBean(name = "jwtUtil")
    private Object jwtUtil;

    @MockBean(name = "jwtFilter")
    private Object jwtFilter;

    @Test
    @DisplayName("팝업 리스트 조회 - 기본 요청")
    void getPopupList_기본요청_200응답() throws Exception {
        // given
        PopupListResponseDto response = PopupListResponseDto.builder()
                .popups(Arrays.asList(createMockSummaryDto(1L, "팝업1")))
                .totalPages(1)
                .totalElements(1L)
                .currentPage(0)
                .size(20)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(popupService.getPopupList(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/popups"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.popups").isArray())
                .andExpect(jsonPath("$.popups", hasSize(1)))
                .andExpect(jsonPath("$.popups[0].id").value(1))
                .andExpect(jsonPath("$.popups[0].title").value("팝업1"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 상태 필터")
    void getPopupList_상태필터_정상응답() throws Exception {
        // given
        PopupListResponseDto response = PopupListResponseDto.builder()
                .popups(Arrays.asList(createMockSummaryDto(1L, "진행중 팝업")))
                .totalPages(1)
                .totalElements(1L)
                .currentPage(0)
                .size(20)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(popupService.getPopupList(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/popups")
                        .param("status", "ONGOING")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popups", hasSize(1)))
                .andExpect(jsonPath("$.popups[0].title").value("진행중 팝업"));
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 지역 및 정렬 필터")
    void getPopupList_복합필터_정상응답() throws Exception {
        // given
        PopupListResponseDto response = PopupListResponseDto.builder()
                .popups(Arrays.asList(
                        createMockSummaryDto(1L, "강남 팝업1"),
                        createMockSummaryDto(2L, "강남 팝업2")
                ))
                .totalPages(1)
                .totalElements(2L)
                .currentPage(0)
                .size(20)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(popupService.getPopupList(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/popups")
                        .param("region", "강남구")
                        .param("sortBy", "latest")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popups", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 날짜 필터")
    void getPopupList_날짜필터_정상응답() throws Exception {
        // given
        PopupListResponseDto response = PopupListResponseDto.builder()
                .popups(Collections.emptyList())
                .totalPages(0)
                .totalElements(0L)
                .currentPage(0)
                .size(20)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(popupService.getPopupList(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/popups")
                        .param("dateFilter", "custom")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-07")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popups").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 잘못된 페이지 파라미터")
    void getPopupList_잘못된페이지_400응답() throws Exception {
        // when & then
        mockMvc.perform(get("/api/popups")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("팝업 상세 조회 - 정상 케이스")
    void getPopupDetail_유효한ID_팝업상세반환() throws Exception {
        // given
        Long popupId = 1L;
        PopupDetailResponseDto response = createMockDetailDto(popupId, "상세 팝업");

        when(popupService.getPopupDetail(popupId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/popups/{popupId}", popupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(popupId))
                .andExpect(jsonPath("$.title").value("상세 팝업"))
                .andExpect(jsonPath("$.status").value("ONGOING"))
                .andExpect(jsonPath("$.statusDisplayText").value("진행 중"))
                .andExpect(jsonPath("$.venueName").exists())
                .andExpect(jsonPath("$.venueAddress").exists())
                .andExpect(jsonPath("$.images").isArray())
                .andExpect(jsonPath("$.hours").isArray());
    }

    @Test
    @DisplayName("팝업 상세 조회 - 존재하지 않는 ID")
    void getPopupDetail_존재하지않는ID_404응답() throws Exception {
        // given
        Long invalidId = 999L;
        when(popupService.getPopupDetail(invalidId))
                .thenThrow(new PopupNotFoundException(invalidId));

        // when & then
        mockMvc.perform(get("/api/popups/{popupId}", invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("팝업 상세 조회 - 잘못된 ID 형식")
    void getPopupDetail_잘못된ID형식_400응답() throws Exception {
        // when & then
        mockMvc.perform(get("/api/popups/{popupId}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("추천 팝업 조회 - 기본 요청")
    void getFeaturedPopups_기본요청_정상응답() throws Exception {
        // given
        PopupListResponseDto response = PopupListResponseDto.builder()
                .popups(Arrays.asList(
                        createMockSummaryDto(1L, "추천 팝업1"),
                        createMockSummaryDto(2L, "추천 팝업2")
                ))
                .totalPages(1)
                .totalElements(2L)
                .currentPage(0)
                .size(20)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(popupService.getFeaturedPopups(anyInt(), anyInt())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/popups/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popups", hasSize(2)))
                .andExpect(jsonPath("$.popups[0].title").value("추천 팝업1"))
                .andExpect(jsonPath("$.popups[1].title").value("추천 팝업2"));
    }

    @Test
    @DisplayName("추천 팝업 조회 - 페이징 파라미터")
    void getFeaturedPopups_페이징파라미터_정상응답() throws Exception {
        // given
        PopupListResponseDto response = PopupListResponseDto.builder()
                .popups(Collections.emptyList())
                .totalPages(0)
                .totalElements(0L)
                .currentPage(1)
                .size(5)
                .hasNext(false)
                .hasPrevious(true)
                .build();

        when(popupService.getFeaturedPopups(1, 5)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/popups/featured")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    // Helper methods
    private PopupSummaryResponseDto createMockSummaryDto(Long id, String title) {
        return PopupSummaryResponseDto.builder()
                .id(id)
                .title(title)
                .summary("테스트 요약")
                .period("2024.01.01 - 2024.01.08")
                .status(PopupStatus.ONGOING)
                .mainImageUrl("test-image.jpg")
                .isFeatured(false)
                .reservationAvailable(false)
                .waitlistAvailable(false)
                .entryFee(0)
                .isFreeEntry(true)
                .feeDisplayText("무료")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .images(Collections.emptyList())
                .venueName("테스트 장소")
                .venueAddress("테스트 주소")
                .region("강남구")
                .parkingAvailable(false)
                .build();
    }

    private PopupDetailResponseDto createMockDetailDto(Long id, String title) {
        return PopupDetailResponseDto.builder()
                .id(id)
                .title(title)
                .summary("테스트 상세 요약")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .periodText("2024.01.01 - 2024.01.08")
                .status(PopupStatus.ONGOING)
                .statusDisplayText("진행 중")
                .mainImageUrl("test-detail-image.jpg")
                .isFeatured(true)
                .reservationAvailable(true)
                .waitlistAvailable(false)
                .entryFee(5000)
                .isFreeEntry(false)
                .feeDisplayText("5,000원")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .venueName("테스트 상세 장소")
                .venueAddress("테스트 상세 주소")
                .region("강남구")
                .parkingAvailable(true)
                .images(Collections.emptyList())
                .hours(Collections.emptyList())
                .build();
    }
}