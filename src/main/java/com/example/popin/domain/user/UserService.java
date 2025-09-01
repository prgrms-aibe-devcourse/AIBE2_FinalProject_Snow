package com.example.popin.domain.user;

import com.example.popin.domain.user.constant.AuthProvider;
import com.example.popin.domain.user.constant.Role;
import com.example.popin.domain.user.dto.SignupRequest;
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

    @Transactional
    public void signup(SignupRequest req){

        validateEmailNotExists(req.getEmail());

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

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)){
            throw new GeneralException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

}
