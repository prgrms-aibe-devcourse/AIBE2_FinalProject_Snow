package com.example.popin.domain.user;

import com.example.popin.domain.user.entity.User;
import com.example.popin.global.constant.ErrorCode;
import com.example.popin.global.exception.GeneralException;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void updateProfile(Long userId, String name, String nickname, String phone){

        User user = findById(userId);
        user.updateProfile(name, nickname, phone);

    }


    @Transactional
    public void changePassword(Long userId, String newPassword){

        User user = findById(userId);
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedPassword);

    }




}
