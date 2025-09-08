package com.snow.popin.domain.popup.entity;

import com.snow.popin.global.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "popups")
@Getter
public class Popup extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_id")
    private Long brandId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    private String title;
    private String summary;
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private PopupStatus status;

    @Column(name = "entry_fee")
    private Integer entryFee = 0;

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

    public String getVenueName() {
        return venue != null ? venue.getName() : null;
    }

    public String getVenueAddress() {
        return venue != null ? venue.getFullAddress() : null;
    }

    public String getRegion() {
        return venue != null ? venue.getRegion() : null;
    }

    public Double getLatitude() {
        return venue != null ? venue.getLatitude() : null;
    }

    public Double getLongitude() {
        return venue != null ? venue.getLongitude() : null;
    }

    public Boolean getParkingAvailable() {
        return venue != null ? venue.getParkingAvailable() : false;
    }

    public String getPeriodText() {
        if (startDate == null && endDate == null) {
            return "기간 미정";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        if (startDate != null && endDate != null) {
            if (startDate.equals(endDate)) {
                return startDate.format(formatter);
            }
            return startDate.format(formatter) + " - " + endDate.format(formatter);
        } else if (startDate != null) {
            return startDate.format(formatter) + " - ";
        } else {
            return " - " + endDate.format(formatter);
        }
    }

    public boolean isOngoing() {
        LocalDate now = LocalDate.now();
        return (startDate == null || !now.isBefore(startDate)) &&
                (endDate == null || !now.isAfter(endDate));
    }

    public boolean isUpcoming() {
        LocalDate now = LocalDate.now();
        return startDate != null && now.isBefore(startDate);
    }

    public boolean isEnded() {
        LocalDate now = LocalDate.now();
        return endDate != null && now.isAfter(endDate);
    }
}