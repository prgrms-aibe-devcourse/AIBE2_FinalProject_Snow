package com.snow.popin.domain.mypage.host.entity;

import com.snow.popin.global.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "brands")
public class Brand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String officialSite;

    @Column(length = 500)
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String snsLinks; // JSON 형태로 저장

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType = BusinessType.INDIVIDUAL;

    @Column
    private Long categoryId;

    public enum BusinessType {
        INDIVIDUAL, CORPORATE
    }
}