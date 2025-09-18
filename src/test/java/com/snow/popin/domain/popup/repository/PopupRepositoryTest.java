package com.snow.popin.domain.popup.repository;

import com.snow.popin.domain.map.entity.Venue;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.testdata.PopupTestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(PopupRepositoryTest.TestConfig.class)
class PopupRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PopupRepository popupRepository;

    @Test
    @DisplayName("상태 필터로 팝업 조회")
    void findWithFilters_상태필터_테스트() {
        // given
        Venue venue = PopupTestDataBuilder.createVenue("강남구");
        entityManager.persistAndFlush(venue);

        Popup ongoingPopup = PopupTestDataBuilder.createPopup("진행중 팝업", PopupStatus.ONGOING, venue);
        Popup plannedPopup = PopupTestDataBuilder.createPopup("예정 팝업", PopupStatus.PLANNED, venue);
        entityManager.persistAndFlush(ongoingPopup);
        entityManager.persistAndFlush(plannedPopup);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.findWithFilters(
                PopupStatus.ONGOING, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(PopupStatus.ONGOING);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("진행중 팝업");
    }

    @Test
    @DisplayName("지역 필터로 팝업 조회")
    void findWithFilters_지역필터_테스트() {
        // given
        Venue venueGangnam = PopupTestDataBuilder.createVenue("강남구");
        Venue venueJongno = PopupTestDataBuilder.createVenue("종로구");
        entityManager.persistAndFlush(venueGangnam);
        entityManager.persistAndFlush(venueJongno);

        Popup popupGangnam = PopupTestDataBuilder.createPopup("강남 팝업", PopupStatus.ONGOING, venueGangnam);
        Popup popupJongno = PopupTestDataBuilder.createPopup("종로 팝업", PopupStatus.ONGOING, venueJongno);
        entityManager.persistAndFlush(popupGangnam);
        entityManager.persistAndFlush(popupJongno);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.findWithFilters(
                null, "강남구", null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVenue().getRegion()).isEqualTo("강남구");
    }

    @Test
    @DisplayName("날짜 범위로 팝업 조회")
    void findWithFilters_날짜필터_테스트() {
        // given
        Venue venue = PopupTestDataBuilder.createVenue("강남구");
        entityManager.persistAndFlush(venue);

        LocalDate today = LocalDate.now();
        LocalDate future = today.plusDays(10);

        Popup currentPopup = PopupTestDataBuilder.createPopupWithDates("현재 팝업", today.minusDays(5), today.plusDays(5), venue);
        Popup futurePopup = PopupTestDataBuilder.createPopupWithDates("미래 팝업", future, future.plusDays(7), venue);
        entityManager.persistAndFlush(currentPopup);
        entityManager.persistAndFlush(futurePopup);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.findWithFilters(
                null, null, today, today.plusDays(7), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("현재 팝업");
    }

    @Test
    @DisplayName("마감임박 팝업 조회")
    void findDeadlineSoonPopups_테스트() {
        // given
        Venue venue = PopupTestDataBuilder.createVenue("강남구");
        entityManager.persistAndFlush(venue);

        LocalDate today = LocalDate.now();
        LocalDate soonDeadline = today.plusDays(3);
        LocalDate farDeadline = today.plusDays(10);

        Popup soonPopup = PopupTestDataBuilder.createPopupWithDates("임박 팝업", today.minusDays(5), soonDeadline, venue);
        soonPopup.setStatusForTest(PopupStatus.ONGOING);
        Popup farPopup = PopupTestDataBuilder.createPopupWithDates("여유 팝업", today.minusDays(5), farDeadline, venue);
        farPopup.setStatusForTest(PopupStatus.ONGOING);

        entityManager.persistAndFlush(soonPopup);
        entityManager.persistAndFlush(farPopup);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.findDeadlineSoonPopups(today.plusDays(7), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("임박 팝업");
    }

    @Test
    @DisplayName("팝업 상세 조회 - 연관 엔티티 포함")
    void findByIdWithDetails_테스트() {
        // given
        Venue venue = PopupTestDataBuilder.createVenue("강남구");
        entityManager.persistAndFlush(venue);

        Popup popup = PopupTestDataBuilder.createPopup("상세 팝업", PopupStatus.ONGOING, venue);
        entityManager.persistAndFlush(popup);

        // when
        Optional<Popup> result = popupRepository.findByIdWithDetails(popup.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getVenue()).isNotNull();
        assertThat(result.get().getVenue().getRegion()).isEqualTo("강남구");
    }

    @Test
    @DisplayName("추천 팝업 조회")
    void findFeaturedPopups_테스트() {
        // given
        Venue venue = PopupTestDataBuilder.createVenue("강남구");
        entityManager.persistAndFlush(venue);

        Popup featuredPopup = PopupTestDataBuilder.createFeaturedPopup("추천 팝업", PopupStatus.ONGOING, venue);
        Popup normalPopup = PopupTestDataBuilder.createPopup("일반 팝업", PopupStatus.ONGOING, venue);

        entityManager.persistAndFlush(featuredPopup);
        entityManager.persistAndFlush(normalPopup);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.findPopularPopups(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIsFeatured()).isTrue();
    }

    @Test
    @DisplayName("상태 업데이트 대상 조회 - PLANNED에서 ONGOING으로")
    void findPopupsToUpdateToOngoing_테스트() {
        // given
        Venue venue = PopupTestDataBuilder.createVenue("강남구");
        entityManager.persistAndFlush(venue);

        LocalDate today = LocalDate.now();
        // 대상: 오늘 시작, 어제 시작
        Popup popupToStartToday = PopupTestDataBuilder.createPopupWithDates("오늘 시작", today, today.plusDays(5), venue);
        popupToStartToday.setStatusForTest(PopupStatus.PLANNED);
        Popup popupStartedYesterday = PopupTestDataBuilder.createPopupWithDates("어제 시작", today.minusDays(1), today.plusDays(5), venue);
        popupStartedYesterday.setStatusForTest(PopupStatus.PLANNED);

        // 비대상: 내일 시작, 이미 ONGOING 상태
        Popup popupStartsTomorrow = PopupTestDataBuilder.createPopupWithDates("내일 시작", today.plusDays(1), today.plusDays(5), venue);
        popupStartsTomorrow.setStatusForTest(PopupStatus.PLANNED);
        Popup ongoingPopup = PopupTestDataBuilder.createPopupWithDates("진행중", today, today.plusDays(5), venue);
        ongoingPopup.setStatusForTest(PopupStatus.ONGOING);

        entityManager.persist(popupToStartToday);
        entityManager.persist(popupStartedYesterday);
        entityManager.persist(popupStartsTomorrow);
        entityManager.persist(ongoingPopup);
        entityManager.flush();

        // when
        List<Popup> result = popupRepository.findPopupsToUpdateToOngoing(today);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("title").containsExactlyInAnyOrder("오늘 시작", "어제 시작");
    }

    @Test
    @DisplayName("상태 업데이트 대상 조회 - ONGOING에서 ENDED로")
    void findPopupsToUpdateToEnded_테스트() {
        // given
        Venue venue = PopupTestDataBuilder.createVenue("종로구");
        entityManager.persistAndFlush(venue);

        LocalDate today = LocalDate.now();
        // 대상: 어제 종료
        Popup popupEndedYesterday = PopupTestDataBuilder.createPopupWithDates("어제 종료", today.minusDays(10), today.minusDays(1), venue);
        popupEndedYesterday.setStatusForTest(PopupStatus.ONGOING);

        // 비대상: 오늘 종료, 이미 ENDED 상태
        Popup popupEndsToday = PopupTestDataBuilder.createPopupWithDates("오늘 종료", today.minusDays(10), today, venue);
        popupEndsToday.setStatusForTest(PopupStatus.ONGOING);
        Popup endedPopup = PopupTestDataBuilder.createPopupWithDates("이미 종료", today.minusDays(10), today.minusDays(1), venue);
        endedPopup.setStatusForTest(PopupStatus.ENDED);

        entityManager.persist(popupEndedYesterday);
        entityManager.persist(popupEndsToday);
        entityManager.persist(endedPopup);
        entityManager.flush();

        // when
        List<Popup> result = popupRepository.findPopupsToUpdateToEnded(today);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("어제 종료");
    }

    @TestConfiguration
    @EnableJpaAuditing
    static class TestConfig {
        @Bean
        public AuditorAware<String> auditorProvider() {
            return () -> Optional.of("test-user");
        }
    }
}