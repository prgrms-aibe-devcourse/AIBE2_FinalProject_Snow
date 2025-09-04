package com.example.popin.domain.mpg_provider.repository;

import com.example.popin.domain.mpg_provider.entity.ProviderProfile;
import com.example.popin.domain.space.entity.Space;
import com.example.popin.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {
    List<Space> findByUserEmail(User owner);
}
