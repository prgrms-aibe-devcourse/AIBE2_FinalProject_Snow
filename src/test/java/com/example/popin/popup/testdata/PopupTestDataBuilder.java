package com.example.popin.popup.testdata;

import com.example.popin.domain.popup.entity.Popup;
import com.example.popin.domain.popup.entity.PopupHours;
import com.example.popin.domain.popup.entity.PopupImage;
import com.example.popin.domain.popup.entity.PopupStatus;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class PopupTestDataBuilder {

    public static Popup createPopup(String title, PopupStatus status, Integer entryFee) {
        Popup popup = new Popup();
        popup.setTitle(title);
        popup.setSummary(title + " 요약");
        popup.setDescription(title + " 상세 설명입니다.");
        popup.setPeriod("2025-09-01 ~ 2025-09-30");
        popup.setStatus(status);
        popup.setEntryFee(entryFee);
        popup.setReservationAvailable(entryFee > 0); // 유료면 예약 가능
        popup.setReservationLink(entryFee > 0 ? "https://booking.example.com" : null);
        popup.setWaitlistAvailable(false);
        popup.setNotice("공지사항입니다.");
        popup.setMainImageUrl("https://example.com/main.jpg");
        popup.setIsFeatured(entryFee == 0); // 무료면 추천
        popup.setBrandId(1L);
        popup.setVenueId(1L);
        return popup;
    }

    public static PopupImage createPopupImage(Popup popup, String imageUrl, String caption, Integer sortOrder) {
        PopupImage image = new PopupImage();
        image.setPopup(popup);
        image.setImageUrl(imageUrl);
        image.setCaption(caption);
        image.setSortOrder(sortOrder);
        return image;
    }

    public static PopupHours createPopupHours(Popup popup, Integer dayOfWeek, String openTime, String closeTime) {
        PopupHours hours = new PopupHours();
        hours.setPopup(popup);
        hours.setDayOfWeek(dayOfWeek);
        hours.setOpenTime(LocalTime.parse(openTime));
        hours.setCloseTime(LocalTime.parse(closeTime));
        hours.setNote(dayOfWeek < 5 ? "평일" : "주말");
        return hours;
    }

    public static Popup createPopupWithImages(String title, PopupStatus status, Integer entryFee) {
        Popup popup = createPopup(title, status, entryFee);

        Set<PopupImage> images = new LinkedHashSet<>();
        images.add(createPopupImage(popup, "https://example.com/image1.jpg", "메인 이미지", 0));
        images.add(createPopupImage(popup, "https://example.com/image2.jpg", "서브 이미지", 1));
        popup.setImages(images);

        return popup;
    }

    public static Popup createPopupWithHours(String title, PopupStatus status, Integer entryFee) {
        Popup popup = createPopup(title, status, entryFee);

        Set<PopupHours> hours = new LinkedHashSet<>();
        // 평일 (월~금)
        for (int i = 0; i < 5; i++) {
            hours.add(createPopupHours(popup, i, "10:00", "20:00"));
        }
        // 주말 (토~일)
        for (int i = 5; i < 7; i++) {
            hours.add(createPopupHours(popup, i, "11:00", "19:00"));
        }
        popup.setHours(hours);

        return popup;
    }

    public static Popup createCompletePopup(String title, PopupStatus status, Integer entryFee) {
        Popup popup = createPopup(title, status, entryFee);

        // 이미지 추가
        Set<PopupImage> images = new LinkedHashSet<>();
        images.add(createPopupImage(popup, "https://example.com/image1.jpg", "메인 이미지", 0));
        images.add(createPopupImage(popup, "https://example.com/image2.jpg", "서브 이미지", 1));
        popup.setImages(images);

        // 운영시간 추가
        Set<PopupHours> hours = new LinkedHashSet<>();
        for (int i = 0; i < 7; i++) {
            if (i < 5) { // 평일
                hours.add(createPopupHours(popup, i, "10:00", "20:00"));
            } else { // 주말
                hours.add(createPopupHours(popup, i, "11:00", "19:00"));
            }
        }
        popup.setHours(hours);

        return popup;
    }
}