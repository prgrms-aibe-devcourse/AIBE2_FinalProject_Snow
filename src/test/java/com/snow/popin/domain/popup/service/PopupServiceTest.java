package com.snow.popin.domain.popup.service;

import com.snow.popin.domain.popup.dto.request.PopupListRequestDto;
import com.snow.popin.domain.popup.dto.request.PopupSearchRequestDto;
import com.snow.popin.domain.popup.dto.response.PopupDetailResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupListResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupSummaryResponseDto;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.entity.Tag;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.popup.testdata.PopupTestDataBuilder;
import com.snow.popin.global.exception.PopupNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("PopupService 테스트")
public class PopupServiceTest {

    @Mock
    private PopupRepository popupRepository;

    @InjectMocks
    private PopupService popupService;

    private Popup samplePopup1;
    private Popup samplePopup2;
    private Popup detailPopup;

    // 검색용 테스트 데이터
    private Popup seoulCafePopup;
    private Popup busanArtPopup;
    private Tag cafeTag;
    private Tag artTag;

    // Reflection을 사용한 필드 설정 헬퍼 메서드
    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("필드 설정 실패: " + fieldName, e);
        }
    }

    @BeforeEach
    void setUp() {
        samplePopup1 = PopupTestDataBuilder.createCompletePopup("무료 팝업", PopupStatus.ONGOING, 0);
        setField(samplePopup1, "id", 1L);

        samplePopup2 = PopupTestDataBuilder.createCompletePopup("유료 팝업", PopupStatus.ONGOING, 10000);
        setField(samplePopup2, "id", 2L);

        detailPopup = PopupTestDataBuilder.createCompletePopup("상세 팝업", PopupStatus.ONGOING, 5000);
        setField(detailPopup, "id", 3L);

        // 검색용 테스트 데이터 설정
        cafeTag = PopupTestDataBuilder.createTag("카페");
        setField(cafeTag, "id", 1L);

        artTag = PopupTestDataBuilder.createTag("아트");
        setField(artTag, "id", 2L);

        seoulCafePopup = PopupTestDataBuilder.createCompletePopup("서울 카페 팝업", PopupStatus.ONGOING, 3000);
        setField(seoulCafePopup, "id", 4L);

        // venue의 region 설정
        setField(seoulCafePopup.getVenue(), "region", "서울");

        Set<Tag> cafeTagSet = new HashSet<>();
        cafeTagSet.add(cafeTag);
        setField(seoulCafePopup, "tags", cafeTagSet);

        busanArtPopup = PopupTestDataBuilder.createCompletePopup("부산 아트 갤러리", PopupStatus.ONGOING, 10000);
        setField(busanArtPopup, "id", 5L);

        // venue의 region 설정
        setField(busanArtPopup.getVenue(), "region", "부산");

        Set<Tag> artTagSet = new HashSet<>();
        artTagSet.add(artTag);
        setField(busanArtPopup, "tags", artTagSet);
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 전체")
    void getPopupList_All() {
        // given
        List<Popup> popups = Arrays.asList(samplePopup1, samplePopup2);
        Page<Popup> popupPage = new PageImpl<>(popups, PageRequest.of(0, 20), 2);

        PopupListRequestDto request = new PopupListRequestDto();
        setField(request, "status", null); // 전체 조회

        given(popupRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .willReturn(popupPage);

        // when
        PopupListResponseDto response = popupService.getPopupList(request);

        // then
        assertThat(response.getPopups()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(20);

        PopupSummaryResponseDto firstPopup = response.getPopups().get(0);
        assertThat(firstPopup.getTitle()).isEqualTo("무료 팝업");
        assertThat(firstPopup.getIsFreeEntry()).isTrue();
        assertThat(firstPopup.getFeeDisplayText()).isEqualTo("무료");
        assertThat(firstPopup.getImages()).hasSize(2);
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 상태별")
    void getPopupList_ByStatus() {
        // given
        List<Popup> popups = Arrays.asList(samplePopup1);
        Page<Popup> popupPage = new PageImpl<>(popups, PageRequest.of(0, 20), 1);

        PopupListRequestDto request = new PopupListRequestDto();
        setField(request, "status", PopupStatus.ONGOING);

        given(popupRepository.findByStatusOrderByCreatedAtDesc(eq(PopupStatus.ONGOING), any(Pageable.class)))
                .willReturn(popupPage);

        // when
        PopupListResponseDto response = popupService.getPopupList(request);

        // then
        assertThat(response.getPopups()).hasSize(1);
        assertThat(response.getPopups().get(0).getStatus()).isEqualTo(PopupStatus.ONGOING);
    }

    @Test
    @DisplayName("팝업 검색 - 제목으로 검색")
    void searchPopups_ByTitle() {
        // given
        List<Popup> popups = Arrays.asList(seoulCafePopup);
        Page<Popup> popupPage = new PageImpl<>(popups, PageRequest.of(0, 20), 1);

        PopupSearchRequestDto request = new PopupSearchRequestDto();
        setField(request, "title", "카페");

        given(popupRepository.searchPopups(eq("카페"), eq(null), any(Pageable.class)))
                .willReturn(popupPage);

        // when
        PopupListResponseDto response = popupService.searchPopups(request);

        // then
        assertThat(response.getPopups()).hasSize(1);
        assertThat(response.getPopups().get(0).getTitle()).isEqualTo("서울 카페 팝업");
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("팝업 검색 - 지역으로 검색")
    void searchPopups_ByRegion() {
        // given
        List<Popup> popups = Arrays.asList(seoulCafePopup);
        Page<Popup> popupPage = new PageImpl<>(popups, PageRequest.of(0, 20), 1);

        PopupSearchRequestDto request = new PopupSearchRequestDto();
        setField(request, "region", "서울");

        given(popupRepository.searchPopups(eq(null), eq("서울"), any(Pageable.class)))
                .willReturn(popupPage);

        // when
        PopupListResponseDto response = popupService.searchPopups(request);

        // then
        assertThat(response.getPopups()).hasSize(1);
        assertThat(response.getPopups().get(0).getTitle()).isEqualTo("서울 카페 팝업");
    }

    @Test
    @DisplayName("팝업 검색 - 태그로 검색")
    void searchPopups_ByTags() {
        // given
        List<Popup> popups = Arrays.asList(seoulCafePopup);
        Page<Popup> popupPage = new PageImpl<>(popups, PageRequest.of(0, 20), 1);

        PopupSearchRequestDto request = new PopupSearchRequestDto();
        setField(request, "tags", Arrays.asList("카페"));

        given(popupRepository.searchPopupsByTags(eq(Arrays.asList("카페")), eq(null), eq(null), any(Pageable.class)))
                .willReturn(popupPage);

        // when
        PopupListResponseDto response = popupService.searchPopups(request);

        // then
        assertThat(response.getPopups()).hasSize(1);
        assertThat(response.getPopups().get(0).getTitle()).isEqualTo("서울 카페 팝업");
    }

    @Test
    @DisplayName("팝업 검색 - 여러 태그로 검색")
    void searchPopups_ByMultipleTags() {
        // given
        List<Popup> popups = Arrays.asList(seoulCafePopup, busanArtPopup);
        Page<Popup> popupPage = new PageImpl<>(popups, PageRequest.of(0, 20), 2);

        PopupSearchRequestDto request = new PopupSearchRequestDto();
        setField(request, "tags", Arrays.asList("카페", "아트"));

        given(popupRepository.searchPopupsByTags(eq(Arrays.asList("카페", "아트")), eq(null), eq(null), any(Pageable.class)))
                .willReturn(popupPage);

        // when
        PopupListResponseDto response = popupService.searchPopups(request);

        // then
        assertThat(response.getPopups()).hasSize(2);
        assertThat(response.getPopups())
                .extracting(PopupSummaryResponseDto::getTitle)
                .containsExactly("서울 카페 팝업", "부산 아트 갤러리");
    }

    @Test
    @DisplayName("팝업 검색 - 복합 조건 검색 (제목 + 지역)")
    void searchPopups_Multiple() {
        // given
        List<Popup> popups = Arrays.asList(seoulCafePopup);
        Page<Popup> popupPage = new PageImpl<>(popups, PageRequest.of(0, 20), 1);

        PopupSearchRequestDto request = new PopupSearchRequestDto();
        setField(request, "title", "카페");
        setField(request, "region", "서울");

        given(popupRepository.searchPopups(eq("카페"), eq("서울"), any(Pageable.class)))
                .willReturn(popupPage);

        // when
        PopupListResponseDto response = popupService.searchPopups(request);

        // then
        assertThat(response.getPopups()).hasSize(1);
        assertThat(response.getPopups().get(0).getTitle()).isEqualTo("서울 카페 팝업");
    }

    @Test
    @DisplayName("팝업 검색 - 태그 + 지역 복합 조건")
    void searchPopups_TagsWithRegion() {
        // given
        List<Popup> popups = Arrays.asList(seoulCafePopup);
        Page<Popup> popupPage = new PageImpl<>(popups, PageRequest.of(0, 20), 1);

        PopupSearchRequestDto request = new PopupSearchRequestDto();
        setField(request, "tags", Arrays.asList("카페"));
        setField(request, "region", "서울");

        given(popupRepository.searchPopupsByTags(eq(Arrays.asList("카페")), isNull(), eq("서울"), any(Pageable.class)))
                .willReturn(popupPage);

        // when
        PopupListResponseDto response = popupService.searchPopups(request);

        // then
        assertThat(response.getPopups()).hasSize(1);
        assertThat(response.getPopups().get(0).getTitle()).isEqualTo("서울 카페 팝업");
    }

    @Test
    @DisplayName("팝업 검색 - 검색 결과 없음")
    void searchPopups_NoResults() {
        // given
        Page<Popup> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);

        PopupSearchRequestDto request = new PopupSearchRequestDto();
        setField(request, "title", "존재하지않는팝업");

        given(popupRepository.searchPopups(eq("존재하지않는팝업"), isNull(), any(Pageable.class)))
                .willReturn(emptyPage);

        // when
        PopupListResponseDto response = popupService.searchPopups(request);

        // then
        assertThat(response.getPopups()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("팝업 상세 조회 - 성공")
    void getPopupDetail_Success() {
        // given
        Long popupId = 3L;
        given(popupRepository.findByIdWithDetails(popupId))
                .willReturn(Optional.of(detailPopup));

        // when
        PopupDetailResponseDto response = popupService.getPopupDetail(popupId);

        // then
        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getTitle()).isEqualTo("상세 팝업");
        assertThat(response.getDescription()).isEqualTo("상세 팝업 상세 설명입니다.");
        assertThat(response.getEntryFee()).isEqualTo(5000);
        assertThat(response.getIsFreeEntry()).isFalse();
        assertThat(response.getFeeDisplayText()).isEqualTo("5,000원");
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.getHours()).hasSize(7);
        assertThat(response.getReservationAvailable()).isTrue();
    }

    @Test
    @DisplayName("팝업 상세 조회 - 존재하지 않는 팝업")
    void getPopupDetail_NotFound() {
        // given
        Long popupId = 999L;
        given(popupRepository.findByIdWithDetails(popupId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> popupService.getPopupDetail(popupId))
                .isInstanceOf(PopupNotFoundException.class)
                .hasMessage("팝업을 찾을 수 없습니다: " + popupId);
    }
}
