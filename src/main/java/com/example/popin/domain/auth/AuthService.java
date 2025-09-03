package com.example.popin.domain.auth;

import com.example.popin.domain.auth.constant.AuthProvider;
import com.example.popin.domain.auth.dto.LoginRequest;
import com.example.popin.domain.auth.dto.LoginResponse;
import com.example.popin.domain.user.constant.Role;
import com.example.popin.domain.auth.dto.SignupRequest;
import com.example.popin.domain.user.entity.User;
import com.example.popin.domain.user.UserRepository;
import com.example.popin.global.constant.ErrorCode;
import com.example.popin.global.exception.GeneralException;
import com.example.popin.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(SignupRequest req){

        if (emailExists(req.getEmail())){
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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->{
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
                });

        return createUserDetails(user);
    }

    private UserDetails createUserDetails(User user) {

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getRole() != null){
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }

        // User가 겹쳐서 full package name
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

    }

    public LoginResponse login(LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new GeneralException(ErrorCode.BAD_REQUEST, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtUtil.createToken(
                user.getId(),
                user.getName(),
                user.getName(),
                user.getRole().name()
        );

        log.info("로그인 성공: {}", request.getEmail());

        return LoginResponse.of(
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );

    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

}
