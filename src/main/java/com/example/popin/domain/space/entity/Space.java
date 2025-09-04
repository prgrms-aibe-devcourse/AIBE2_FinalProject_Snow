package com.example.popin.domain.space.entity;

import com.example.popin.domain.user.entity.User;
import com.example.popin.global.common.BaseEntity;
import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "place_lists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Space extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "area_size")
    private Integer areaSize;


    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "is_official", nullable = false)
    private Boolean isOfficial = false;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(length = 500)
    private String address;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "rental_fee")
    private Integer rentalFee;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Builder
    public Space(User owner, String title, String description,
                 String address, Integer areaSize, LocalDate startDate, LocalDate endDate,
                 Integer rentalFee, String contactPhone, String coverImageUrl) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.address = address;
        this.areaSize = areaSize;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentalFee = rentalFee;
        this.contactPhone = contactPhone;
        this.coverImageUrl = coverImageUrl;
        this.isPublic = true;
        this.isOfficial = false;
    }

    // 비즈니스 메서드
    public void updateSpaceInfo(String title, String description, String address,
                                Integer areaSize, LocalDate startDate, LocalDate endDate,
                                Integer rentalFee, String contactPhone) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.areaSize = areaSize;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentalFee = rentalFee;
        this.contactPhone = contactPhone;
    }

    public void updateCoverImage(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public boolean isOwner(User user) {
        return this.owner.getId().equals(user.getId());
    }

    public boolean isAvailableDate(LocalDate checkDate) {
        return !checkDate.isBefore(startDate) && !checkDate.isAfter(endDate);
    }
}