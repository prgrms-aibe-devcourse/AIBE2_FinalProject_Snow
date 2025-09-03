package com.example.popin.domain.space.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SpaceSearchDto {
    private String keyword;
    private LocalDate availableStartDate;
    private LocalDate availableEndDate;
    private Integer minPrice;
    private Integer maxPrice;
    private String address;
    private Integer areaSize;

}