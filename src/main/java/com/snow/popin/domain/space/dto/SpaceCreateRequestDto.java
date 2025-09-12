package com.snow.popin.domain.space.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SpaceCreateRequestDto {

    @NotBlank(message = "공간명(임대인)은 필수입니다.")
    @Size(max = 200, message = "공간명은 200자를 초과할 수 없습니다.")
    private String title;

    @Size(max = 1000, message = "상세 설명은 1000자를 초과할 수 없습니다.")
    private String description;

    @NotNull(message = "면적은 필수입니다.")
    @Positive(message = "면적은 0보다 커야 합니다.")
    private Integer areaSize;

    @NotNull(message = "임대 시작일은 필수입니다.")
    @FutureOrPresent(message = "시작일은 오늘 이후여야 합니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull(message = "임대 종료일은 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @NotNull(message = "임대료는 필수입니다.")
    @Min(value = 0, message = "임대료는 0원 이상이어야 합니다.")
    @Max(value = 999999999, message = "임대료는 너무 클 수 없습니다.")
    private Integer rentalFee;

    @Pattern(regexp = "^[0-9-+()\\s]+$", message = "올바른 전화번호 형식이 아닙니다.")
    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다.")
    private String contactPhone;

    private MultipartFile image;

    //  Venue 관련 필드
    @Size(max = 255, message = "도로명 주소는 255자를 초과할 수 없습니다.")
    private String roadAddress;

    @Size(max = 255, message = "지번 주소는 255자를 초과할 수 없습니다.")
    private String jibunAddress;

    @Size(max = 255, message = "상세 주소는 255자를 초과할 수 없습니다.")
    private String detailAddress;

    private Double latitude;   // 좌표 (API 붙이면 활용)
    private Double longitude;

    private Boolean parkingAvailable;

    // 유효성 검증 메서드
    @AssertTrue(message = "종료일은 시작일 이후여야 합니다.")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }
}
