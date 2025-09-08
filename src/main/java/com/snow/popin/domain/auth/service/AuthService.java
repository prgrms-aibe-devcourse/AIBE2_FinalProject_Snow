package com.snow.popin.domain.auth.service;

import com.snow.popin.domain.auth.constant.AuthProvider;
import com.snow.popin.domain.auth.dto.*;
import com.snow.popin.domain.user.constant.Role;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.domain.user.repository.UserRepository;
import com.snow.popin.global.constant.ErrorCode;
import com.snow.popin.global.exception.GeneralException;
import com.snow.popin.global.jwt.JwtTokenResolver;
import com.snow.popin.global.jwt.JwtUtil;
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

import javax.servlet.http.Cookie;
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

    // ================ 회원가입 관련 ================
    // 회원가입 처리
    @Transactional
    public SignupResponse signup(SignupRequest req) {

        log.info("회원가입 시도: {}", req.getEmail());

        try {
            // 1. 비밀번호 확인 검증
            if (!req.isPasswordMatching()){
                throw new GeneralException(ErrorCode.BAD_REQUEST,"비밀번호가 일치하지 않습니다.");
            }

            // 2. 중복 검증
            validateDuplicates(req);

            // 3. 사용자 생성 및 저장
            User uesr = createUser(req);
            User savedUser = userRepository.save(uesr);

            // 4. 관심사 처리 (나중에 카테고리 엔티티 완성되면 추가)
            // processUserInterests(savedUser.getId(), req.getInterestCategoryIds());

            log.info("회원가입 성공: {}", req.getEmail());
            return SignupResponse.success(savedUser.getEmail(), savedUser.getName(), savedUser.getNickname());
        } catch (GeneralException e){
            log.warn("회원가입 실패: {} - {}", req.getEmail(), e.getMessage());
            throw e;
        }  catch (Exception e) {
            log.error("회원가입 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new GeneralException(ErrorCode.INTERNAL_ERROR, "회원가입 처리 중 오류가 발생했습니다.");
        }

    }

    // 중복 검증
    private void validateDuplicates(SignupRequest req){
        if (userRepository.existsByEmail(req.getEmail())){
            throw new GeneralException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(req.getNickname())){
            throw new GeneralException(ErrorCode.BAD_REQUEST, "이미 사용 중인 닉네임입니다.");
        }
    }

    // 사용자 엔티티 생성
    private User createUser(SignupRequest req){
        String encodedPassword = passwordEncoder.encode(req.getPassword());

        return User.builder()
                .email(req.getEmail())
                .password(encodedPassword)
                .name(req.getName())
                .nickname(req.getNickname())
                .phone(req.getPhone())
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();
    }

    // ================ 로그인 관련 ================
    // 사용자 로그인 처리
    public LoginResponse login(LoginRequest req) {
        log.info("로그인 시도: {}", req.getEmail());

        try {
            // 1. 사용자 자격 증명 검증
            User user = validateUserCredentials(req);

            // 2. JWT 토큰 생성
            String accessToken = generateAccessToken(user);

            // 3. 로그인 응답 생성
            LoginResponse response = createLoginResponse(user, accessToken);

            // 4. 성공 로깅
            log.info("로그인 성공: {}", user.getEmail());

            return response;

        } catch (GeneralException e) {
            logFailedLogin(req.getEmail(), e);
            throw e;
        } catch (Exception e) {
            log.error("로그인 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new GeneralException(ErrorCode.INTERNAL_ERROR, "로그인 처리 중 오류가 발생했습니다.");
        }
    }


    // 사용자 자격 증명 검증
    private User validateUserCredentials(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 이메일로 로그인 시도: {}", req.getEmail());
                    return new GeneralException(ErrorCode.LOGIN_FAILED);
                });

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("잘못된 비밀번호로 로그인 시도: {}", req.getEmail());
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
        } catch (Exception e) {
            log.error("JWT 토큰 생성 실패 - 사용자: {}, 오류: {}", user.getEmail(), e.getMessage(), e);
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

    // 실패한 로그인 후처리
    private void logFailedLogin(String email, Exception e) {
        log.warn("로그인 실패: {} - {}", email, e.getMessage());
    }

    // ================ 로그아웃 관련 ================
    // 사용자 로그아웃 처리
    public LogoutResponse logout(LogoutRequest req, HttpServletRequest httpReq, HttpServletResponse httpRes) {
        String userEmail = "unknown";

        try {
            String token = extractToken(req, httpReq);

            // 토큰에서 사용자 정보 추출 (로깅용)
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                userEmail = jwtUtil.getEmail(token);
            }

            log.info("로그아웃 요청 처리: {}", userEmail);

            // 로그아웃 처리
            processLogout(token, httpReq, httpRes);

            log.info("로그아웃 완료: {}", userEmail);
            return LogoutResponse.success("로그아웃이 완료되었습니다.");

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 - 사용자: {}, 오류: {}", userEmail, e.getMessage(), e);
            // 로그아웃은 항상 성공으로 처리 (클라이언트에서 토큰 정리)
            return LogoutResponse.success("로그아웃이 완료되었습니다.");
        }
    }

    // 실제 로그아웃 처리 로직
    private void processLogout(String token, HttpServletRequest req, HttpServletResponse res) {
        try {
            // 토큰 검증 및 로깅
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                String userEmail = jwtUtil.getEmail(token);
                log.info("사용자 로그아웃 처리: {}", userEmail);
            } else {
                log.debug("유효하지 않은 토큰으로 로그아웃 시도");
            }

            // 쿠키 정리
            clearAuthCookies(res);

            // 캐시 정리 헤더 추가
            addCacheControlHeaders(res);

        } catch (Exception e) {
            log.warn("로그아웃 처리 중 오류 발생 (계속 진행): {}", e.getMessage());
        }
    }

    // 인증 관련 쿠키들 정리
    private void clearAuthCookies(HttpServletResponse res) {
        clearCookie(res, "jwtToken", "/");
        clearCookie(res, "JSESSIONID", "/");
        clearCookie(res, "remember-me", "/");
    }

    // 개별 쿠키 정리
    private void clearCookie(HttpServletResponse res, String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath(path);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 개발환경: false, 프로덕션: true
        res.addCookie(cookie);
        log.debug("쿠키 삭제: {}", name);
    }

    // 캐시 제어 헤더 추가
    private void addCacheControlHeaders(HttpServletResponse res) {
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");
    }

    // 요청에서 토큰 추출
    private String extractToken(LogoutRequest req, HttpServletRequest httpReq) {
        // 1. 요청 바디에서 토큰 확인
        if (StringUtils.hasText(req.getAccessToken())) {
            return req.getAccessToken();
        }

        // 2. 헤더/쿠키에서 토큰 확인
        return jwtTokenResolver.resolve(httpReq);
    }

    // ================ Spring Security 연동 ================
    // Spring Security UserDetailsService 구현
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return createUserDetails(user);
    }

    // Spring Security UserDetails 객체 생성
    private UserDetails createUserDetails(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }

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

    // ================ 기타 ================
    // 이메일 중복 확인
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // 닉네임 중복 확인
    public boolean nicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}