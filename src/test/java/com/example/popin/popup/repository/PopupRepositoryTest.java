package com.example.popin.popup.repository;

import com.example.popin.domain.popup.entity.Popup;
import com.example.popin.domain.popup.entity.PopupStatus;
import com.example.popin.domain.popup.entity.Tag;
import com.example.popin.domain.popup.repository.PopupRepository;
import com.example.popin.domain.popup.repository.TagRepository;
import com.example.popin.popup.testdata.PopupTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional  // 각 테스트 후 롤백
@DisplayName("PopupRepository 테스트")
class PopupRepositoryTest {

    @Autowired
    private PopupRepository popupRepository;

    @Autowired
    private TagRepository tagRepository;

    private Popup ongoingPopup1;
    private Popup ongoingPopup2;
    private Popup plannedPopup;
    private Popup endedPopup;

    // 검색용 테스트 데이터
    private Popup seoulCafePopup;
    private Popup busanArtPopup;
    private Popup seoulRestaurantPopup;
    private Tag cafeTag;
    private Tag artTag;
    private Tag restaurantTag;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        popupRepository.deleteAll();
        tagRepository.deleteAll();

        // 태그 생성
        cafeTag = new Tag();
        cafeTag.setName("카페");
        cafeTag = tagRepository.save(cafeTag);

        artTag = new Tag();
        artTag.setName("아트");
        artTag = tagRepository.save(artTag);

        restaurantTag = new Tag();
        restaurantTag.setName("레스토랑");
        restaurantTag = tagRepository.save(restaurantTag);

        // 기본 테스트 데이터 생성 및 저장
        ongoingPopup1 = PopupTestDataBuilder.createCompletePopup("진행중 팝업1", PopupStatus.ONGOING, 0);
        ongoingPopup1.setRegion("경기");
        popupRepository.save(ongoingPopup1);

        ongoingPopup2 = PopupTestDataBuilder.createCompletePopup("진행중 팝업2", PopupStatus.ONGOING, 5000);
        ongoingPopup2.setRegion("인천");
        popupRepository.save(ongoingPopup2);

        plannedPopup = PopupTestDataBuilder.createCompletePopup("계획된 팝업", PopupStatus.PLANNED, 3000);
        plannedPopup.setRegion("대전");
        popupRepository.save(plannedPopup);

        endedPopup = PopupTestDataBuilder.createCompletePopup("종료된 팝업", PopupStatus.ENDED, 0);
        endedPopup.setRegion("광주");
        popupRepository.save(endedPopup);

        // 검색용 테스트 데이터 생성
        seoulCafePopup = PopupTestDataBuilder.createCompletePopup("서울 카페 팝업", PopupStatus.ONGOING, 3000);
        seoulCafePopup.setRegion("서울");
        Set<Tag> cafeTagSet = new HashSet<>();
        cafeTagSet.add(cafeTag);
        seoulCafePopup.setTags(cafeTagSet);
        seoulCafePopup = popupRepository.save(seoulCafePopup);

        busanArtPopup = PopupTestDataBuilder.createCompletePopup("부산 아트 갤러리", PopupStatus.ONGOING, 10000);
        busanArtPopup.setRegion("부산");
        Set<Tag> artTagSet = new HashSet<>();
        artTagSet.add(artTag);
        busanArtPopup.setTags(artTagSet);
        busanArtPopup = popupRepository.save(busanArtPopup);

        seoulRestaurantPopup = PopupTestDataBuilder.createCompletePopup("서울 레스토랑 팝업", PopupStatus.ONGOING, 15000);
        seoulRestaurantPopup.setRegion("서울");
        Set<Tag> restaurantTagSet = new HashSet<>();
        restaurantTagSet.add(restaurantTag);
        seoulRestaurantPopup.setTags(restaurantTagSet);
        seoulRestaurantPopup = popupRepository.save(seoulRestaurantPopup);
    }

    @Test
    @DisplayName("상태별 팝업 조회 - ONGOING")
    void findByStatusOrderByCreatedAtDesc_Ongoing() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.findByStatusOrderByCreatedAtDesc(PopupStatus.ONGOING, pageable);

        // then
        assertThat(result.getContent()).hasSize(5); // ongoingPopup1, ongoingPopup2 + 3개 검색용 데이터
        assertThat(result.getContent())
                .extracting(Popup::getStatus)
                .containsOnly(PopupStatus.ONGOING);
    }

    @Test
    @DisplayName("전체 팝업 조회")
    void findAllByOrderByCreatedAtDesc() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.findAllByOrderByCreatedAtDesc(pageable);

        // then
        assertThat(result.getContent()).hasSize(7); // 총 7개
        assertThat(result.getTotalElements()).isEqualTo(7);
    }

    @Test
    @DisplayName("팝업 검색 - 제목으로 검색")
    void searchPopups_ByTitle() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.searchPopups("카페", null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("서울 카페 팝업");
    }

    @Test
    @DisplayName("팝업 검색 - 지역으로 검색")
    void searchPopups_ByRegion() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.searchPopups(null, "서울", pageable);

        // then
        assertThat(result.getContent()).hasSize(2); // 서울 카페, 서울 레스토랑
        assertThat(result.getContent())
                .extracting(Popup::getRegion)
                .containsOnly("서울");
    }

    @Test
    @DisplayName("팝업 검색 - 제목과 지역 복합 검색")
    void searchPopups_ByTitleAndRegion() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.searchPopups("카페", "서울", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("서울 카페 팝업");
        assertThat(result.getContent().get(0).getRegion()).isEqualTo("서울");
    }

    @Test
    @DisplayName("팝업 검색 - 태그로 검색")
    void searchPopupsByTags_SingleTag() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.searchPopupsByTags(
                Arrays.asList("카페"), null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("서울 카페 팝업");
    }

    @Test
    @DisplayName("팝업 검색 - 여러 태그로 검색")
    void searchPopupsByTags_MultipleTags() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.searchPopupsByTags(
                Arrays.asList("카페", "아트"), null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2); // 카페 팝업 + 아트 팝업
        assertThat(result.getContent())
                .extracting(Popup::getTitle)
                .containsExactlyInAnyOrder("서울 카페 팝업", "부산 아트 갤러리");
    }

    @Test
    @DisplayName("팝업 검색 - 태그와 지역 복합 검색")
    void searchPopupsByTags_WithRegion() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.searchPopupsByTags(
                Arrays.asList("카페", "레스토랑"), null, "서울", pageable);

        // then
        assertThat(result.getContent()).hasSize(2); // 서울 카페, 서울 레스토랑
        assertThat(result.getContent())
                .extracting(Popup::getRegion)
                .containsOnly("서울");
    }

    @Test
    @DisplayName("팝업 검색 - 태그와 제목 복합 검색")
    void searchPopupsByTags_WithTitle() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.searchPopupsByTags(
                Arrays.asList("카페", "레스토랑"), "카페", null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1); // 제목에 "카페"가 포함된 것만
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("서울 카페 팝업");
    }

    @Test
    @DisplayName("팝업 검색 - 검색 결과 없음")
    void searchPopups_NoResults() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Popup> result = popupRepository.searchPopups("존재하지않는팝업", null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("상세 조회 - 이미지와 운영시간 포함")
    void findByIdWithDetails() {
        // when
        Optional<Popup> result = popupRepository.findByIdWithDetails(ongoingPopup1.getId());

        // then
        assertThat(result).isPresent();
        Popup popup = result.get();
        assertThat(popup.getTitle()).isEqualTo("진행중 팝업1");
        assertThat(popup.getImages()).hasSize(2);
        assertThat(popup.getHours()).hasSize(7);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 상세 조회")
    void findByIdWithDetails_NotFound() {
        // when
        Optional<Popup> result = popupRepository.findByIdWithDetails(999L);

        // then
        assertThat(result).isEmpty();
    }
}