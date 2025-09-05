package com.snow.popin.popup.service;

import com.snow.popin.domain.popup.dto.request.PopupListRequestDto;
import com.snow.popin.domain.popup.dto.request.PopupSearchRequestDto;
import com.snow.popin.domain.popup.dto.response.PopupDetailResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupListResponseDto;
import com.snow.popin.domain.popup.dto.response.PopupSummaryResponseDto;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.entity.Tag;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.popup.service.PopupService;
import com.snow.popin.global.exception.PopupNotFoundException;
import com.snow.popin.popup.testdata.PopupTestDataBuilder;
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

    @BeforeEach
    void setUp() {
        samplePopup1 = PopupTestDataBuilder.createCompletePopup("무료 팝업", PopupStatus.ONGOING, 0);
        samplePopup1.setId(1L);

        samplePopup2 = PopupTestDataBuilder.createCompletePopup("유료 팝업", PopupStatus.ONGOING, 10000);
        samplePopup2.setId(2L);

        detailPopup = PopupTestDataBuilder.createCompletePopup("상세 팝업", PopupStatus.ONGOING, 5000);
        detailPopup.setId(3L);

        // 검색용 테스트 데이터 설정
        cafeTag = new Tag();
        cafeTag.setId(1L);
        cafeTag.setName("카페");

        artTag = new Tag();
        artTag.setId(2L);
        artTag.setName("아트");

        seoulCafePopup = PopupTestDataBuilder.createCompletePopup("서울 카페 팝업", PopupStatus.ONGOING, 3000);
        seoulCafePopup.setId(4L);
        seoulCafePopup.setRegion("서울");
        Set<Tag> cafeTagSet = new HashSet<>();
        cafeTagSet.add(cafeTag);
        seoulCafePopup.setTags(cafeTagSet);

        busanArtPopup = PopupTestDataBuilder.createCompletePopup("부산 아트 갤러리", PopupStatus.ONGOING, 10000);
        busanArtPopup.setId(5L);
        busanArtPopup.setRegion("부산");
        Set<Tag> artTagSet = new HashSet<>();
        artTagSet.add(artTag);
        busanArtPopup.setTags(artTagSet);
    }

    @Test
    @DisplayName("팝업 리스트 조회 - 전체")
    void getPopupList_All() {
        // given
        List<Popup> popups = Arrays.asList(samplePopup1, samplePopup2);
        Page<Popup> popupPage = new PageImpl<>(popups, PageRequest.of(0, 20), 2);

        PopupListRequestDto request = new PopupListRequestDto();
        request.setStatus(null); // 전체 조회

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
        request.setStatus(PopupStatus.ONGOING);

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
        request.setTitle("카페");

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
        request.setRegion("서울");

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
        request.setTags(Arrays.asList("카페"));

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
        request.setTags(Arrays.asList("카페", "아트"));

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
        request.setTitle("카페");
        request.setRegion("서울");

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
        request.setTags(Arrays.asList("카페"));
        request.setRegion("서울");

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
        request.setTitle("존재하지않는팝업");

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