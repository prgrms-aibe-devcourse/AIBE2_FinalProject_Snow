package com.snow.popin.domain.auth.service;

import com.snow.popin.domain.auth.constant.AuthProvider;
import com.example.popin.domain.auth.dto.*;
import com.example.popin.domain.user.constant.Role;
import com.example.popin.domain.user.entity.User;
import com.example.popin.domain.user.UserRepository;
import com.example.popin.global.constant.ErrorCode;
import com.example.popin.global.exception.GeneralException;
import com.snow.popin.global.jwt.JwtTokenResolver;
import com.snow.popin.global.jwt.JwtUtil;
import com.snow.popin.domain.auth.dto.*;
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
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final JwtTokenResolver jwtTokenResolver;

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

    public LoginResponse login(LoginRequest req){

        log.info("로그인 시도 : {}", req.getEmail());

        try {

            // 1. 사용자 자격 증명 검증
            User user = validateUserCredentials(req);

            // 2. JWT 토큰 생성
            String accessToken = generateAccessToken(user);

            // 3. 로그인 응답 생성
            LoginResponse res = createLoginResponse(user, accessToken);

            // 4. 성공 로깅
            log.info("로그인 성공: {}", user.getEmail());

            return res;

        } catch (Exception e){
            log.error("로그인 처리 중 예상치 못한 오류 : {}", e.getMessage(), e);
            throw new GeneralException(ErrorCode.INTERNAL_ERROR, "로그인 처리 중 오류가 발생했습니다.");
        }
    }

    // 사용자 자격 증명 검증
    private User validateUserCredentials(LoginRequest req) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 이메일로 로그인 시도 : {}", req.getEmail());
                    return new GeneralException(ErrorCode.LOGIN_FAILED);
                });

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())){
            log.warn("잘못된 비밀번호로 로그인 시도 : {}", req.getEmail());
            throw new GeneralException(ErrorCode.LOGIN_FAILED);
        }

        return user;

    }

    // JWT 액세스 토큰 생성
    private String generateAccessToken(User user) {
        try {
            return jwtUtil.createToken(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole().name()
            );
        } catch (Exception e){
            log.error("JWT 토큰 생성 실패 - 사용자 : {}, 오류 : {}", user.getEmail(), e.getMessage(), e);
            throw new GeneralException(ErrorCode.INTERNAL_ERROR, "인증 토큰 생성에 실패했습니다.");
        }
    }

    // 로그인 응답 객체 생성
    private LoginResponse createLoginResponse(User user, String accessToken) {
        return LoginResponse.of(
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }

    public LogoutResponse logout(LogoutRequest req, HttpServletRequest httpReq, HttpServletResponse httpRes){
        try{
            String token = req.getAccessToken();

            if (!StringUtils.hasText(token)){
                token = jwtTokenResolver.resolve(httpReq);
            }

            logoutService.processLogout(token, httpReq, httpRes);

            return LogoutResponse.success("로그아웃이 완료되었습니다.");
        } catch (Exception e){
            log.error("로그아웃 처리 중 오류 발생 : {}", e.getMessage(), e);
            return LogoutResponse.success("로그아웃이 완료되었습니다."); // 로그아웃은 항상 성공으로 처리
        }
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

}
