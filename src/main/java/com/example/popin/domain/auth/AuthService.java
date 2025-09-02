package com.example.popin.domain.auth;

import com.example.popin.domain.auth.constant.AuthProvider;
import com.example.popin.domain.user.constant.Role;
import com.example.popin.domain.auth.dto.SignupRequest;
import com.example.popin.domain.user.entity.User;
import com.example.popin.domain.user.UserRepository;
import com.example.popin.global.constant.ErrorCode;
import com.example.popin.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest req){

        if (!emailExists(req.getEmail())){
            throw new GeneralException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(req.getPassword());

        User user = User.builder()
                .email(req.getEmail())
                .password(encodedPassword)
                .name(req.getName())
                .nickname(req.getNickname())
                .phone(req.getPhone())
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    public boolean emailExists(String email) {
        return !userRepository.existsByEmail(email);
    }

}
