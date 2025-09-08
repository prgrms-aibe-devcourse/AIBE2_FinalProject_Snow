package com.snow.popin.domain.popup.controller;

import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.entity.Tag;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.popup.repository.TagRepository;
import com.snow.popin.domain.popup.testdata.PopupTestDataBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("PopupController 통합 테스트")
public class PopupControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PopupRepository popupRepository;

    @Autowired
    private TagRepository tagRepository;

    private MockMvc mockMvc;

    private Long savedPopupId1;
    private Long savedPopupId2;
    private Long savedPopupId3;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 기존 데이터 정리 (혹시 남아있을 수 있는 데이터)
        popupRepository.deleteAll();
        tagRepository.deleteAll();

        // 기본 테스트 데이터 생성
        Popup popup1 = PopupTestDataBuilder.createCompletePopup("무료 팝업스토어", PopupStatus.ONGOING, 0);
        savedPopupId1 = popupRepository.save(popup1).getId();

        Popup popup2 = PopupTestDataBuilder.createCompletePopup("유료 팝업스토어", PopupStatus.ONGOING, 8000);
        savedPopupId2 = popupRepository.save(popup2).getId();

        Popup popup3 = PopupTestDataBuilder.createCompletePopup("계획된 팝업스토어", PopupStatus.PLANNED, 5000);
        savedPopupId3 = popupRepository.save(popup3).getId();

        // 검색용 태그 생성
        Tag cafeTag = tagRepository.save(PopupTestDataBuilder.createTag("카페"));
        Tag artTag = tagRepository.save(PopupTestDataBuilder.createTag("아트"));
        Tag exhibitionTag = tagRepository.save(PopupTestDataBuilder.createTag("전시"));

        // 검색용 팝업 생성
        Popup seoulCafePopup = PopupTestDataBuilder.builder()
                .title("서울 카페 팝업")
                .region("서울")
                .addTag(cafeTag)
                .build();
        popupRepository.save(seoulCafePopup);

        Popup busanArtPopup = PopupTestDataBuilder.builder()
                .title("부산 아트 전시")
                .region("부산")
                .tags(Set.of(artTag, exhibitionTag))
                .build();
        popupRepository.save(busanArtPopup);

        Popup seoulArtPopup = PopupTestDataBuilder.builder()
                .title("서울 아트 팝업")
                .region("서울")
                .addTag(artTag)
                .build();
        popupRepository.save(seoulArtPopup);
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
                .andExpect(jsonPath("$.popups.length()").value(6))
                .andExpect(jsonPath("$.totalElements").value(6))
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
                .andExpect(jsonPath("$.popups.length()").value(5))
                .andExpect(jsonPath("$.popups[0].status").value("ONGOING"));
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
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(3))
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
    @Transactional(readOnly = true)
    void getPopupDetail_Success() throws Exception {
        mockMvc.perform(get("/api/popups/{popupId}", savedPopupId1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPopupId1))
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
    @Transactional(readOnly = true)
    void getPopupDetail_PaidPopup() throws Exception {
        mockMvc.perform(get("/api/popups/{popupId}", savedPopupId2))
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

    @Test
    @DisplayName("팝업 검색 - 제목으로 검색")
    void searchPopups_ByTitle() throws Exception {
        mockMvc.perform(get("/api/popups/search")
                        .param("title", "카페"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.popups[0].title").value("서울 카페 팝업"));
    }

    @Test
    @DisplayName("팝업 검색 - 지역으로 검색")
    void searchPopups_ByRegion() throws Exception {
        mockMvc.perform(get("/api/popups/search")
                        .param("region", "서울"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.popups[*].title", containsInAnyOrder("서울 카페 팝업", "서울 아트 팝업")));
    }

    @Test
    @DisplayName("팝업 검색 - 태그로 검색")
    void searchPopups_ByTag() throws Exception {
        mockMvc.perform(get("/api/popups/search")
                        .param("tags", "전시"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.popups[0].title").value("부산 아트 전시"));
    }

    @Test
    @DisplayName("팝업 검색 - 제목, 지역, 태그 복합 검색")
    void searchPopups_ByAllConditions() throws Exception {
        mockMvc.perform(get("/api/popups/search")
                        .param("title", "아트")
                        .param("region", "부산")
                        .param("tags", "전시"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.popups[0].title").value("부산 아트 전시"));
    }

    @Test
    @DisplayName("팝업 검색 - 검색 결과 없음")
    void searchPopups_NoResult() throws Exception {
        mockMvc.perform(get("/api/popups/search")
                        .param("title", "존재하지 않는 팝업"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.popups").isEmpty());
    }

    @Test
    @DisplayName("팝업 검색 - 빈 문자열 파라미터 처리")
    void searchPopups_WithBlankParameters() throws Exception {
        mockMvc.perform(get("/api/popups/search")
                        .param("title", "")
                        .param("region", "   "))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(6));
    }
}