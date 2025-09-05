package com.snow.popin.domain.popup.entity;

import com.snow.popin.global.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "popup_images")
@Getter
@Setter
public class PopupImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "popup_id", nullable = false)
    private Popup popup;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    private String caption;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @PrePersist
    void prePersist() {
        if (sortOrder == null) sortOrder = 0;
    }
}