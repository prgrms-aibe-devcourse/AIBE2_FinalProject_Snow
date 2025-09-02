package com.example.popin.popup.repository;

import com.example.popin.domain.popup.entity.Popup;
import com.example.popin.domain.popup.entity.PopupStatus;
import com.example.popin.domain.popup.repository.PopupRepository;
import com.example.popin.popup.testdata.PopupTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@DisplayName("PopupRepository 테스트")
class PopupRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PopupRepository popupRepository;

    private Popup ongoingPopup1;
    private Popup ongoingPopup2;
    private Popup plannedPopup;
    private Popup endedPopup;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        ongoingPopup1 = PopupTestDataBuilder.createCompletePopup("진행중 팝업1", PopupStatus.ONGOING, 0);
        ongoingPopup2 = PopupTestDataBuilder.createCompletePopup("진행중 팝업2", PopupStatus.ONGOING, 5000);
        plannedPopup = PopupTestDataBuilder.createCompletePopup("계획된 팝업", PopupStatus.PLANNED, 3000);
        endedPopup = PopupTestDataBuilder.createCompletePopup("종료된 팝업", PopupStatus.ENDED, 0);

        entityManager.persistAndFlush(ongoingPopup1);
        entityManager.persistAndFlush(ongoingPopup2);
        entityManager.persistAndFlush(plannedPopup);
        entityManager.persistAndFlush(endedPopup);
    }

    @Test
    @DisplayName("상태별 팝업 조회 - ONGOING")
    void findByStatusOrderByCreatedAtDesc_Ongoing() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.findByStatusOrderByCreatedAtDesc(PopupStatus.ONGOING, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Popup::getStatus)
                .containsOnly(PopupStatus.ONGOING);
        assertThat(result.getContent().get(0).getTitle()).contains("진행중 팝업");
    }

    @Test
    @DisplayName("전체 팝업 조회")
    void findAllByOrderByCreatedAtDesc() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.findAllByOrderByCreatedAtDesc(pageable);

        // then
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("상세 조회 - 이미지와 운영시간 포함")
    void findByIdWithDetails() {
        // when
        Popup result = popupRepository.findByIdWithDetails(ongoingPopup1.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("진행중 팝업1");
        assertThat(result.getImages()).hasSize(2);
        assertThat(result.getHours()).hasSize(7);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 상세 조회")
    void findByIdWithDetails_NotFound() {
        // when
        Popup result = popupRepository.findByIdWithDetails(999L);

        // then
        assertThat(result).isNull();
    }
}
