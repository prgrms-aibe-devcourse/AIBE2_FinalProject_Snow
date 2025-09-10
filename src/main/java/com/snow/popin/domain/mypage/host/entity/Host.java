package com.snow.popin.domain.mypage.host.entity;

import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.common.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(
        name = "brand_members",
        indexes = {
                @Index(name = "idx_brand_member_user_id", columnList = "user_id"),
                @Index(name = "idx_brand_member_brand_id", columnList = "brand_id")
        },
        uniqueConstraints = {
                // 유저는 하나의 브랜드만 소속 가능하도록
                @UniqueConstraint(columnNames = {"user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Host extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_in_brand", nullable = false)
    private HostRole roleInBrand;

    public boolean isOwner() {
        return this.roleInBrand == HostRole.OWNER;
    }
}
