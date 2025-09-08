package com.snow.popin.domain.mypage.provider.repository;

import com.snow.popin.domain.mypage.provider.entity.ProviderProfile;
import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {
    List<Space> findByUserEmail(User owner);
}
