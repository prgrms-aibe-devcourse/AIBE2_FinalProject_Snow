package com.example.popin.domain.popup.entity;

import com.example.popin.global.common.BaseEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_id")
    private Popup popup;

    @Column(name = "image_url")
    private String imageUrl;

    private String caption;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}