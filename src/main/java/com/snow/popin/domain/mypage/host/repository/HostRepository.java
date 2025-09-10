package com.snow.popin.domain.mypage.host.repository;

import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.mypage.host.entity.Host;
import com.snow.popin.domain.mypage.host.entity.HostRole;
import com.snow.popin.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostRepository extends JpaRepository<Host, Long> {

    //  유저는 하나의 Host만 가질 수 있음
    Optional<Host> findByUser(User user);

    // 특정 브랜드의 모든 멤버
    List<Host> findByBrand(Brand brand);

    // 특정 브랜드-사용자 조합
    Optional<Host> findByBrandAndUser(Brand brand, User user);

    // 특정 브랜드-사용자 조합 존재 여부
    boolean existsByBrandAndUser(Brand brand, User user);

    // 사용자가 OWNER인 브랜드들
    List<Host> findByUserAndRoleInBrand(User user, HostRole role);

    // 특정 브랜드의 OWNER 조회
    List<Host> findByBrandAndRoleInBrand(Brand brand, HostRole role);
}
