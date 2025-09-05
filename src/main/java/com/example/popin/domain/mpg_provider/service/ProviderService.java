// src/main/java/com/example/popin/MPg_Provider/service/ProviderService.java
package com.example.popin.domain.mpg_provider.service;

import com.example.popin.domain.space.entity.Space;
import com.example.popin.domain.space.repository.SpaceRepository;
import com.example.popin.domain.user.entity.User;
import com.example.popin.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProviderService {

    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;

    // '공간대여'에서 등록한 내 공간 리스트
    public List<Space> findMySpaces(String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return spaceRepository.findByOwner(owner);
    }


}
