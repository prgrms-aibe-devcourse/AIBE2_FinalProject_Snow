package com.example.popin.domain.popup.entity;

import com.example.popin.global.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "popups")
@Getter @Setter
public class Popup extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "venue_id")
    private Long venueId;

    private String title;
    private String summary;
    private String description;
    private String period;

    @Enumerated(EnumType.STRING)
    private PopupStatus status;

    @Column(name = "entry_fee")
    private Integer entryFee = 0;

    private String region;
    private String address;
    private Double latitude;
    private Double longitude;

    @Column(name = "reservation_available")
    private Boolean reservationAvailable = false;

    @Column(name = "reservation_link")
    private String reservationLink;

    @Column(name = "waitlist_available")
    private Boolean waitlistAvailable = false;

    private String notice;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @OneToMany(mappedBy = "popup", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 50)
    @OrderBy("sortOrder ASC")
    private Set<PopupImage> images = new LinkedHashSet<>();

    @OneToMany(mappedBy = "popup", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 50)
    @OrderBy("dayOfWeek ASC")
    private Set<PopupHours> hours = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "popup_tags",
            joinColumns = @JoinColumn(name = "popup_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new LinkedHashSet<>();

    public boolean isFreeEntry() {
        return entryFee == null || entryFee == 0;
    }

    public String getFeeDisplayText() {
        return isFreeEntry() ? "무료" : String.format("%,d원", entryFee);
    }
}