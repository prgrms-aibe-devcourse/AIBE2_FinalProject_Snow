package com.snow.popin.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snow.popin.domain.auth.AuthService;
import com.snow.popin.domain.auth.dto.LoginRequest;
import com.snow.popin.domain.auth.dto.LoginResponse;
import com.snow.popin.global.config.SecurityConfig;
import com.snow.popin.global.constant.ErrorCode;
import com.snow.popin.global.exception.GeneralException;
import com.snow.popin.global.jwt.JwtFilter;
import com.snow.popin.global.jwt.JwtUtil;
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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("API 컨트롤러 - 인증")
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

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // JWT 관련 빈들을 MockBean으로 추가 (필요한 경우)
    @MockBean
    private JwtUtil jwtUtil;

    private LoginRequest validLoginRequest;
    private LoginResponse mockLoginResponse;

    public AuthApiControllerTest(@Autowired MockMvc mvc, @Autowired ObjectMapper objectMapper) {
        this.mvc = mvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {
        // 유효한 로그인 요청 데이터
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        // 모킹용 로그인 응답 데이터
        mockLoginResponse = LoginResponse.builder()
                .accessToken("jwt-access-token")
                .tokenType("Bearer")
                .userId(1L)
                .email("test@example.com")
                .name("테스트유저")
                .role("USER")
                .build();
    }

    @DisplayName("[API] 로그인 성공 - 유효한 요청으로 로그인 시 토큰과 사용자 정보 반환")
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("jwt-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andDo(print());
    }

    @DisplayName("[API] 로그인 실패 - 잘못된 이메일 형식으로 요청 시 400 에러 반환")
    @Test
    void givenInvalidEmailFormat_whenLogin_thenReturnsBadRequest() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("invalid-email"); // 잘못된 이메일 형식
        invalidRequest.setPassword("password123");

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("[API] 로그인 실패 - 빈 이메일로 요청 시 400 에러 반환")
    @Test
    void givenEmptyEmail_whenLogin_thenReturnsBadRequest() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail(""); // 빈 이메일
        invalidRequest.setPassword("password123");

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수입니다")))
                .andDo(print());
    }

    @DisplayName("[API] 로그인 실패 - 빈 비밀번호로 요청 시 400 에러 반환")
    @Test
    void givenEmptyPassword_whenLogin_thenReturnsBadRequest() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword(""); // 빈 비밀번호

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수입니다")))
                .andDo(print());
    }

    @DisplayName("[API] 로그인 실패 - password 필드 누락 시 400 에러 반환")
    @Test
    void givenMissingPassword_whenLogin_thenReturnsBadRequest() throws Exception {
        // Given - password 필드가 없는 JSON
        String requestJson = "{\"email\":\"test@example.com\"}";

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수입니다")))
                .andDo(print());
    }

    @DisplayName("[API] 로그인 실패 - 존재하지 않는 사용자로 요청 시 404 에러 반환")
    @Test
    void givenNonExistentUser_whenLogin_thenReturnsNotFound() throws Exception {
        // Given
        willThrow(new GeneralException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .given(authService)
                .login(any(LoginRequest.class));

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @DisplayName("[API] 로그인 실패 - 잘못된 비밀번호로 요청 시 400 에러 반환")
    @Test
    void givenWrongPassword_whenLogin_thenReturnsBadRequest() throws Exception {
        // Given
        willThrow(new GeneralException(ErrorCode.BAD_REQUEST, "이메일 또는 비밀번호가 올바르지 않습니다."))
                .given(authService)
                .login(any(LoginRequest.class));

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @DisplayName("[API] 로그인 실패 - JSON 형식이 잘못된 요청 시 400 에러 반환")
    @Test
    void givenInvalidJsonFormat_whenLogin_thenReturnsBadRequest() throws Exception {
        // Given
        String invalidJson = "{\"email\":\"test@example.com\",\"password\":"; // 잘못된 JSON

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("[API] 이메일 중복 확인 - 사용 가능한 이메일인 경우 available=true 반환")
    @Test
    void givenAvailableEmail_whenCheckEmail_thenReturnsAvailableTrue() throws Exception {
        // Given
        String email = "available@example.com";
        given(authService.emailExists(email)).willReturn(false);

        // When & Then
        mvc.perform(get("/api/auth/check-email")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.exists").value(false))
                .andDo(print());
    }

    @DisplayName("[API] 이메일 중복 확인 - 이미 사용 중인 이메일인 경우 available=false 반환")
    @Test
    void givenExistingEmail_whenCheckEmail_thenReturnsAvailableFalse() throws Exception {
        // Given
        String email = "existing@example.com";
        given(authService.emailExists(email)).willReturn(true);

        // When & Then
        mvc.perform(get("/api/auth/check-email")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.exists").value(true))
                .andDo(print());
    }

    @DisplayName("[API] 이메일 중복 확인 - 이메일 파라미터가 없는 경우 400 에러 반환")
    @Test
    void givenNoEmailParameter_whenCheckEmail_thenReturnsBadRequest() throws Exception {
        // When & Then
        mvc.perform(get("/api/auth/check-email"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("[API] 회원가입 성공 - 유효한 요청으로 회원가입 시 성공 응답 반환")
    @Test
    void givenValidSignupRequest_whenSignup_thenReturnsSuccess() throws Exception {
        // Given
        Map<String, Object> signupData = new HashMap<>();
        signupData.put("email", "newuser@example.com");
        signupData.put("password", "password123");
        signupData.put("name", "신규유저");
        signupData.put("nickname", "뉴비");
        signupData.put("phone", "010-1234-5678");

        String signupJson = objectMapper.writeValueAsString(signupData);

        // When & Then
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("[API] 회원가입 실패 - 이미 존재하는 이메일로 요청 시 400 에러 반환")
    @Test
    void givenDuplicateEmail_whenSignup_thenReturnsBadRequest() throws Exception {
        // Given
        String signupJson = "{\n" +
                "    \"email\": \"duplicate@example.com\",\n" +
                "    \"password\": \"password123\",\n" +
                "    \"name\": \"중복유저\",\n" +
                "    \"nickname\": \"중복\",\n" +
                "    \"phone\": \"010-1234-5678\"\n" +
                "}";

        willThrow(new GeneralException(ErrorCode.DUPLICATE_EMAIL))
                .given(authService)
                .signup(any());

        // When & Then
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.DUPLICATE_EMAIL.getCode()))
                .andDo(print());
    }
}