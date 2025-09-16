package com.snow.popin.domain.space.repository;

import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long>, JpaSpecificationExecutor<Space> {

    // 특정 사용자가 소유한 공간 목록 조회
    List<Space> findByOwnerOrderByCreatedAtDesc(User owner);

    // 특정 사용자 소유의 특정 공간 조회 (권한 체크용)
    Optional<Space> findByIdAndOwner(Long id, User owner);

    // 공간 모두 조회
    List<Space> findByIsPublicTrueOrderByCreatedAtDesc();

    List<Space> findByOwner(User owner);

    List<Space> findByIsPublicTrueAndIsHiddenFalseOrderByCreatedAtDesc();
    //검색용 또는 통계 추가 할 것

    // 통계용 메서드들
    long countByIsPublic(boolean isPublic);
    long countByCreatedAtAfter(LocalDateTime date);
    // N+1 문제 방지를 위한 fetch join 쿼리 (필요시 사용)
    @Query("SELECT s FROM Space s JOIN FETCH s.owner WHERE s.id = :id")
    Optional<Space> findByIdWithOwner(@Param("id") Long id);
}