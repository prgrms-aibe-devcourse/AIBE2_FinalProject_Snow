package com.snow.popin.domain.spacereservation.dto;

import org.springframework.format.annotation.DateTimeFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SpaceReservationCreateRequestDto {

    @NotNull(message = "공간 ID는 필수입니다.")
    private Long spaceId;

    @NotBlank(message = "브랜드명은 필수입니다.")
    @Size(max = 100, message = "브랜드명은 100자를 초과할 수 없습니다.")
    private String brand;

    @NotBlank(message = "팝업명은 필수입니다.")
    @Size(max = 200, message = "팝업명은 200자를 초과할 수 없습니다.")
    private String popupTitle;

    @NotNull(message = "시작일은 필수입니다.")
    @FutureOrPresent(message = "시작일은 오늘 이후여야 합니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다.")
    private String message;

    @Pattern(regexp = "^[0-9-+()\\s]+$", message = "올바른 전화번호 형식이 아닙니다.")
    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다.")
    private String contactPhone;

    @Size(max = 2000, message = "팝업 설명은 2000자를 초과할 수 없습니다.")
    private String popupDescription;
}