package com.snow.popin.domain.popup.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;

@Data
@Builder
public class PopupHoursResponseDto {
    private Long id;
    private Integer dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String note;

    public String getDayOfWeekText() {
        String[] days = {"월", "화", "수", "목", "금", "토", "일"};
        return dayOfWeek != null && dayOfWeek >= 0 && dayOfWeek <= 6 ? days[dayOfWeek] : "";
    }

    public String getTimeRangeText() {
        if (openTime == null && closeTime == null) {
            return "시간 미정";
        }
        return String.format("%s - %s",
                openTime != null ? openTime.toString() : "미정",
                closeTime != null ? closeTime.toString() : "미정");
    }
}