package com.snow.popin.domain.space.repository;

import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {

    // 특정 사용자가 소유한 공간 목록 조회
    List<Space> findByOwnerOrderByCreatedAtDesc(User owner);

    // 특정 사용자 소유의 특정 공간 조회 (권한 체크용)
    Optional<Space> findByIdAndOwner(Long id, User owner);

    // 공간 모두 조회
    List<Space> findByIsPublicTrueOrderByCreatedAtDesc();

    List<Space> findByOwner(User owner);

    List<Space> findByIsPublicTrueAndIsHiddenFalseOrderByCreatedAtDesc();
    //검색용 또는 통계 추가 할 것
}