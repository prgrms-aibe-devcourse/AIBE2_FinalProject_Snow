package com.example.popin.domain.popup.repository;

import com.example.popin.domain.popup.entity.Popup;
import com.example.popin.domain.popup.entity.PopupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long> {

    // 전체 팝업 조회 (상태별)
    Page<Popup> findByStatusOrderByCreatedAtDesc(PopupStatus status, Pageable pageable);

    // 모든 상태의 팝업 조회
    Page<Popup> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 팝업 상세 조회 (이미지, 시간 정보 포함)
    @EntityGraph(attributePaths = {"images", "hours"})
    @Query("SELECT p FROM Popup p WHERE p.id = :id")
    Optional<Popup> findByIdWithDetails(@Param("id") Long id);
}