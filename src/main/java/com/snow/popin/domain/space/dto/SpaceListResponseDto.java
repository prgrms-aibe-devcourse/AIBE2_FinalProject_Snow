package com.snow.popin.domain.space.dto;

import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SpaceListResponseDto {
    private Long id;
    private String ownerName;
    private String title;
    private String address;
    private Integer areaSize;
    private boolean mine;


    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Integer rentalFee;
    private String coverImageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    public static SpaceListResponseDto from(Space space, User me) {
        return SpaceListResponseDto.builder()
                .id(space.getId())
                .title(space.getTitle())
                .ownerName(space.getOwner().getName())
                .address(space.getAddress())
                .areaSize(space.getAreaSize())
                .startDate(space.getStartDate())
                .endDate(space.getEndDate())
                .rentalFee(space.getRentalFee())
                .coverImageUrl(space.getCoverImageUrl())
                .createdAt(space.getCreatedAt())
                .mine(me != null && java.util.Objects.equals(space.getOwner().getId(), me.getId()))
                .build();
    }
}