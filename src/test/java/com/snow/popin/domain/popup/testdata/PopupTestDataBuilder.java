package com.snow.popin.domain.popup.testdata;

import com.snow.popin.domain.map.entity.Venue;
import com.snow.popin.domain.popup.entity.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class PopupTestDataBuilder {

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

    public static Venue createVenue(String name, String region) {
        Venue venue = new Venue();
        setField(venue, "name", name);
        setField(venue, "roadAddress", "서울시 강남구 테헤란로 123");
        setField(venue, "region", region);
        setField(venue, "latitude", 37.5665);
        setField(venue, "longitude", 126.9780);
        setField(venue, "parkingAvailable", true);
        return venue;
    }

    public static Tag createTag(String name) {
        Tag tag = new Tag();
        setField(tag, "name", name);
        return tag;
    }

    public static Popup createPopup(String title, PopupStatus status, Integer entryFee) {
        Popup popup = new Popup();
        Venue venue = createVenue("테스트 장소", "서울");

        setField(popup, "title", title);
        setField(popup, "summary", title + " 요약");
        setField(popup, "description", title + " 상세 설명입니다.");
        setField(popup, "startDate", LocalDate.now());
        setField(popup, "endDate", LocalDate.now().plusDays(30));
        setField(popup, "status", status);
        setField(popup, "entryFee", entryFee);
        setField(popup, "reservationAvailable", entryFee > 0);
        setField(popup, "reservationLink", entryFee > 0 ? "https://booking.example.com" : null);
        setField(popup, "waitlistAvailable", false);
        setField(popup, "notice", "공지사항입니다.");
        setField(popup, "mainImageUrl", "https://example.com/main.jpg");
        setField(popup, "isFeatured", entryFee == 0);
        setField(popup, "brandId", 1L);
        setField(popup, "venue", venue);

        return popup;
    }

    public static PopupImage createPopupImage(Popup popup, String imageUrl, String caption, Integer sortOrder) {
        PopupImage image = new PopupImage();
        setField(image, "popup", popup);
        setField(image, "imageUrl", imageUrl);
        setField(image, "caption", caption);
        setField(image, "sortOrder", sortOrder);
        return image;
    }

    public static PopupHours createPopupHours(Popup popup, Integer dayOfWeek, String openTime, String closeTime) {
        PopupHours hours = new PopupHours();
        setField(hours, "popup", popup);
        setField(hours, "dayOfWeek", dayOfWeek);
        setField(hours, "openTime", LocalTime.parse(openTime));
        setField(hours, "closeTime", LocalTime.parse(closeTime));
        setField(hours, "note", dayOfWeek < 5 ? "평일" : "주말");
        return hours;
    }

    public static Popup createCompletePopup(String title, PopupStatus status, Integer entryFee) {
        Popup popup = createPopup(title, status, entryFee);

        // 이미지 추가
        Set<PopupImage> images = new LinkedHashSet<>();
        images.add(createPopupImage(popup, "https://example.com/image1.jpg", "메인 이미지", 0));
        images.add(createPopupImage(popup, "https://example.com/image2.jpg", "서브 이미지", 1));
        setField(popup, "images", images);

        // 운영시간 추가
        Set<PopupHours> hours = new LinkedHashSet<>();
        for (int i = 0; i < 7; i++) {
            if (i < 5) { // 평일
                hours.add(createPopupHours(popup, i, "10:00", "20:00"));
            } else { // 주말
                hours.add(createPopupHours(popup, i, "11:00", "19:00"));
            }
        }
        setField(popup, "hours", hours);

        return popup;
    }

    // 검색 테스트용 팝업 빌더들
    public static PopupBuilder builder() {
        return new PopupBuilder();
    }

    public static class PopupBuilder {
        private String title = "테스트 팝업";
        private PopupStatus status = PopupStatus.ONGOING;
        private Integer entryFee = 0;
        private String region = "서울";
        private Set<Tag> tags = new LinkedHashSet<>();

        public PopupBuilder title(String title) {
            this.title = title;
            return this;
        }

        public PopupBuilder status(PopupStatus status) {
            this.status = status;
            return this;
        }

        public PopupBuilder entryFee(Integer entryFee) {
            this.entryFee = entryFee;
            return this;
        }

        public PopupBuilder region(String region) {
            this.region = region;
            return this;
        }

        public PopupBuilder addTag(Tag tag) {
            this.tags.add(tag);
            return this;
        }

        public PopupBuilder tags(Set<Tag> tags) {
            this.tags = tags;
            return this;
        }

        public Popup build() {
            Popup popup = createCompletePopup(title, status, entryFee);

            // venue의 region 설정
            Venue venue = popup.getVenue();
            setField(venue, "region", region);

            // 태그 설정
            if (!tags.isEmpty()) {
                setField(popup, "tags", tags);
            }

            return popup;
        }
    }
}