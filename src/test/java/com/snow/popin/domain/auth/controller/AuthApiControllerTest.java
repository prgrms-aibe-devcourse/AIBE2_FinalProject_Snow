package com.snow.popin.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snow.popin.domain.auth.dto.*;
import com.snow.popin.domain.auth.service.AuthService;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.constant.ErrorCode;
import com.snow.popin.global.exception.GeneralException;
import com.snow.popin.global.jwt.JwtUtil;
import com.snow.popin.global.config.SecurityConfig;
import com.snow.popin.global.jwt.JwtFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("API 컨트롤러 - 인증 (로그인/로그아웃/회원가입)")
@WebMvcTest(
        controllers = AuthApiController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtUtil.class)
        }
)
class AuthApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    private LoginRequest validLoginRequest;
    private LoginResponse mockLoginResponse;
    private SignupRequest validSignupRequest;
    private SignupResponse mockSignupResponse;

    @BeforeEach
    void setUp() {
        // 로그인 테스트 데이터
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        mockLoginResponse = LoginResponse.builder()
                .accessToken("jwt-access-token")
                .tokenType("Bearer")
                .userId(1L)
                .email("test@example.com")
                .name("테스트유저")
                .role("USER")
                .build();

        // 회원가입 테스트 데이터
        validSignupRequest = new SignupRequest();
        validSignupRequest.setEmail("newuser@example.com");
        validSignupRequest.setPassword("password123");
        validSignupRequest.setName("신규유저");
        validSignupRequest.setNickname("뉴비");
        validSignupRequest.setPhone("010-1234-5678");

        mockSignupResponse = SignupResponse.builder()
                .success(true)
                .message("회원가입이 완료되었습니다.")
                .email("newuser@example.com")
                .build();
    }

    // ================ 회원가입 테스트 ================

    @DisplayName("[API] 회원가입 성공")
    @Test
    void givenValidSignupRequest_whenSignup_thenReturnsSuccess() throws Exception {
        // Given
        given(authService.signup(any(SignupRequest.class)))
                .willReturn(mockSignupResponse);

        // When & Then
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSignupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @DisplayName("[API] 회원가입 실패 - 이메일 중복")
    @Test
    void givenDuplicateEmail_whenSignup_thenReturnsBadRequest() throws Exception {
        // Given
        willThrow(new GeneralException(ErrorCode.DUPLICATE_EMAIL))
                .given(authService)
                .signup(any(SignupRequest.class));

        // When & Then
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSignupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.DUPLICATE_EMAIL.getCode()));
    }

    @DisplayName("[API] 이메일 중복 확인")
    @Test
    void givenEmail_whenCheckEmailDuplicate_thenReturnsAvailability() throws Exception {
        // Given
        given(authService.emailExists(anyString())).willReturn(false);

        // When & Then
        mvc.perform(get("/api/auth/check-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.exists").value(false));
    }

    @DisplayName("[API] 닉네임 중복 확인")
    @Test
    void givenNickname_whenCheckNicknameDuplicate_thenReturnsAvailability() throws Exception {
        // Given
        given(authService.nicknameExists(anyString())).willReturn(false);

        // When & Then
        mvc.perform(get("/api/auth/check-nickname")
                        .param("nickname", "테스터"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.exists").value(false));
    }

    // ================ 로그인 테스트 ================

    @DisplayName("[API] 로그인 성공")
    @Test
    void givenValidLoginRequest_whenLogin_thenReturnsLoginResponse() throws Exception {
        // Given
        given(authService.login(any(LoginRequest.class)))
                .willReturn(mockLoginResponse);

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @DisplayName("[API] 로그인 실패 - 존재하지 않는 사용자")
    @Test
    void givenNonExistentUser_whenLogin_thenReturnsLoginFailed() throws Exception {
        // Given
        willThrow(new GeneralException(ErrorCode.LOGIN_FAILED))
                .given(authService)
                .login(any(LoginRequest.class));

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.LOGIN_FAILED.getCode()));
    }

    // ================ 로그아웃 테스트 ================

    @DisplayName("[API] 로그아웃 성공")
    @Test
    void givenValidToken_whenLogout_thenReturnsSuccess() throws Exception {
        // Given
        LogoutRequest logoutRequest = new LogoutRequest("valid-token", null);
        LogoutResponse logoutResponse = LogoutResponse.success("로그아웃이 완료되었습니다.");

        given(authService.logout(any(LogoutRequest.class), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .willReturn(logoutResponse);

        // When & Then
        mvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."));
    }

    private void setUserId(User user, Long id) {
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("테스트용 ID 설정 실패", e);
        }
    }
}
