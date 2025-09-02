package com.example.popin.popup.controller;

import com.example.popin.domain.popup.entity.Popup;
import com.example.popin.domain.popup.entity.PopupStatus;
import com.example.popin.domain.popup.repository.PopupRepository;
import com.example.popin.popup.testdata.PopupTestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
@DisplayName("PopupController 통합 테스트")
public class PopupControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PopupRepository popupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Popup savedPopup1;
    private Popup savedPopup2;
    private Popup savedPopup3;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 테스트 데이터 저장
        savedPopup1 = popupRepository.save(
                PopupTestDataBuilder.createCompletePopup("무료 팝업스토어", PopupStatus.ONGOING, 0)
        );
        savedPopup2 = popupRepository.save(
                PopupTestDataBuilder.createCompletePopup("유료 팝업스토어", PopupStatus.ONGOING, 8000)
        );
        savedPopup3 = popupRepository.save(
                PopupTestDataBuilder.createCompletePopup("계획된 팝업스토어", PopupStatus.PLANNED, 5000)
        );
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 전체")
    void getPopupList_All() throws Exception {
        mockMvc.perform(get("/api/popups")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popups").isArray())
                .andExpect(jsonPath("$.popups.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.popups[0].title").exists())
                .andExpect(jsonPath("$.popups[0].entryFee").exists())
                .andExpect(jsonPath("$.popups[0].feeDisplayText").exists())
                .andExpect(jsonPath("$.popups[0].images").isArray());
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 상태별 (ONGOING)")
    void getPopupList_ByStatus() throws Exception {
        mockMvc.perform(get("/api/popups")
                        .param("status", "ONGOING")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popups").isArray())
                .andExpect(jsonPath("$.popups.length()").value(2))
                .andExpect(jsonPath("$.popups[0].status").value("ONGOING"))
                .andExpect(jsonPath("$.popups[1].status").value("ONGOING"));
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 페이징")
    void getPopupList_WithPaging() throws Exception {
        mockMvc.perform(get("/api/popups")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popups.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 정렬")
    void getPopupList_WithSort() throws Exception {
        mockMvc.perform(get("/api/popups")
                        .param("sortBy", "title")
                        .param("sortDirection", "ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popups").isArray());
    }

    @Test
    @DisplayName("팝업 상세 조회 - 성공")
    void getPopupDetail_Success() throws Exception {
        mockMvc.perform(get("/api/popups/{popupId}", savedPopup1.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPopup1.getId()))
                .andExpect(jsonPath("$.title").value("무료 팝업스토어"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.entryFee").value(0))
                .andExpect(jsonPath("$.isFreeEntry").value(true))
                .andExpect(jsonPath("$.feeDisplayText").value("무료"))
                .andExpect(jsonPath("$.reservationAvailable").value(false))
                .andExpect(jsonPath("$.images").isArray())
                .andExpect(jsonPath("$.images.length()").value(2))
                .andExpect(jsonPath("$.hours").isArray())
                .andExpect(jsonPath("$.hours.length()").value(7))
                .andExpect(jsonPath("$.brandId").exists())
                .andExpect(jsonPath("$.venueId").exists());
    }

    @Test
    @DisplayName("팝업 상세 조회 - 유료 팝업")
    void getPopupDetail_PaidPopup() throws Exception {
        mockMvc.perform(get("/api/popups/{popupId}", savedPopup2.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryFee").value(8000))
                .andExpect(jsonPath("$.isFreeEntry").value(false))
                .andExpect(jsonPath("$.feeDisplayText").value("8,000원"))
                .andExpect(jsonPath("$.reservationAvailable").value(true))
                .andExpect(jsonPath("$.reservationLink").exists());
    }

    @Test
    @DisplayName("팝업 상세 조회 - 존재하지 않는 팝업")
    void getPopupDetail_NotFound() throws Exception {
        mockMvc.perform(get("/api/popups/{popupId}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 잘못된 파라미터")
    void getPopupList_InvalidParameters() throws Exception {
        mockMvc.perform(get("/api/popups")
                        .param("page", "-1")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
