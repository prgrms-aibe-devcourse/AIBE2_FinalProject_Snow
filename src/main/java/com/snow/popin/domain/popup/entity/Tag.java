package com.snow.popin.domain.popup.entity;

import com.snow.popin.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Popup> popups;

    public void updateName(String name) {
        this.name = name;
    }
}