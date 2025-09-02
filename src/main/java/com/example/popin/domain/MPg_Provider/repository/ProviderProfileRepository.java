package com.example.popin.domain.MPg_Provider.repository;

import com.example.popin.domain.MPg_Provider.entity.ProviderProfile;
import com.example.popin.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {
    List<Space> findByOwner(User owner);
}
