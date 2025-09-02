package com.example.popin.popup.service;

import com.example.popin.domain.popup.dto.request.PopupListRequestDto;
import com.example.popin.domain.popup.dto.response.PopupDetailResponseDto;
import com.example.popin.domain.popup.dto.response.PopupListResponseDto;
import com.example.popin.domain.popup.dto.response.PopupSummaryResponseDto;
import com.example.popin.domain.popup.entity.Popup;
import com.example.popin.domain.popup.entity.PopupStatus;
import com.example.popin.domain.popup.repository.PopupRepository;
import com.example.popin.domain.popup.service.PopupService;
import com.example.popin.popup.testdata.PopupTestDataBuilder;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @BeforeEach
    void setUp() {
        samplePopup1 = PopupTestDataBuilder.createCompletePopup("무료 팝업", PopupStatus.ONGOING, 0);
        samplePopup1.setId(1L);

        samplePopup2 = PopupTestDataBuilder.createCompletePopup("유료 팝업", PopupStatus.ONGOING, 10000);
        samplePopup2.setId(2L);

        detailPopup = PopupTestDataBuilder.createCompletePopup("상세 팝업", PopupStatus.ONGOING, 5000);
        detailPopup.setId(3L);
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
    @DisplayName("팝업 상세 조회 - 성공")
    void getPopupDetail_Success() {
        // given
        Long popupId = 3L;
        given(popupRepository.findByIdWithDetails(popupId))
                .willReturn(detailPopup);

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
                .willReturn(null);

        // when & then
        assertThatThrownBy(() -> popupService.getPopupDetail(popupId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("팝업을 찾을 수 없습니다: " + popupId);
    }
}
