package com.snow.popin.domain.reward.repository;

import com.snow.popin.domain.reward.entity.RewardOption;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.*;

public interface RewardOptionRepository extends JpaRepository<RewardOption, Long> {

    List<RewardOption> findByMissionSetId(UUID missionSetId);

    /** 재고 차감 시 동시성 제어 (비관적 락) */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from RewardOption o where o.id = :id")
    Optional<RewardOption> lockById(@Param("id") Long id);
}
