package com.snow.popin.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snow.popin.domain.auth.constant.AuthProvider;
import com.snow.popin.domain.auth.dto.LoginRequest;
import com.snow.popin.domain.auth.dto.LogoutRequest;
import com.snow.popin.domain.auth.dto.SignupRequest;
import com.snow.popin.domain.user.repository.UserRepository;
import com.snow.popin.domain.user.constant.Role;
import com.snow.popin.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("인증 통합 테스트 (회원가입/로그인/로그아웃)")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "jwt.secret=rwNscFdjVS7RxDUVt8hkPRImnFUay1kmMy34XvurwjY="
})
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private final String TEST_EMAIL = "integration@example.com";
    private final String TEST_PASSWORD = "testPassword123";

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성 (로그인 테스트용)
        testUser = User.builder()
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .name("통합테스트유저")
                .nickname("통합테스터")
                .phone("010-1111-2222")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        userRepository.save(testUser);
    }

    // ================ 회원가입 테스트 ================

    @DisplayName("[통합] 회원가입 성공")
    @Test
    void givenValidSignupRequest_whenSignup_thenReturnsSuccess() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("newPassword123");
        signupRequest.setPasswordConfirm("newPassword123");
        signupRequest.setName("신규유저");
        signupRequest.setNickname("신규닉네임");
        signupRequest.setPhone("010-9999-8888");

        // When & Then
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @DisplayName("[통합] 회원가입 실패 - 이미 존재하는 이메일")
    @Test
    void givenDuplicateEmail_whenSignup_thenReturnsBadRequest() throws Exception {
        // Given - 이미 존재하는 이메일 사용
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(TEST_EMAIL); // 기존 사용자와 동일한 이메일
        signupRequest.setPassword("newPassword123");
        signupRequest.setPasswordConfirm("newPassword123");
        signupRequest.setName("신규유저");
        signupRequest.setNickname("신규닉네임");
        signupRequest.setPhone("010-9999-8888");

        // When & Then
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andDo(print());
    }

    // ================ 로그인 테스트 ================

    @DisplayName("[통합] 로그인 성공")
    @Test
    void givenValidCredentials_whenLogin_thenReturnsSuccessWithToken() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.name").value("통합테스트유저"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andDo(print());
    }

    @DisplayName("[통합] 로그인 실패 - 잘못된 비밀번호")
    @Test
    void givenWrongPassword_whenLogin_thenReturnsBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword("wrongPassword");

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andDo(print());
    }

    // ================ 로그아웃 테스트 ================

    @DisplayName("[통합] 로그아웃 성공 - 유효한 토큰으로 로그아웃")
    @Test
    void givenValidToken_whenLogout_thenReturnsSuccess() throws Exception {
        // Given - 먼저 로그인해서 토큰 획득
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult loginResult = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // 로그아웃 요청
        LogoutRequest logoutRequest = LogoutRequest.builder()
                .accessToken(accessToken)
                .build();

        // When & Then
        mvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."))
                .andDo(print());
    }

    @DisplayName("[통합] 로그아웃 성공 - 토큰 없이도 성공")
    @Test
    void givenNoToken_whenLogout_thenReturnsSuccess() throws Exception {
        // Given - 빈 LogoutRequest 객체 생성
        LogoutRequest logoutRequest = LogoutRequest.builder().build();

        // When & Then - 토큰이 없어도 로그아웃은 성공해야 함
        mvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(print());
    }

    // ================ 중복 확인 테스트 ================

    @DisplayName("[통합] 이메일 중복 확인 - 사용 가능한 이메일")
    @Test
    void givenAvailableEmail_whenCheckEmail_thenReturnsAvailable() throws Exception {
        // Given
        String availableEmail = "available@example.com";

        // When & Then
        mvc.perform(get("/api/auth/check-email")
                        .param("email", availableEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.exists").value(false))
                .andDo(print());
    }

    @DisplayName("[통합] 이메일 중복 확인 - 이미 사용 중인 이메일")
    @Test
    void givenExistingEmail_whenCheckEmail_thenReturnsNotAvailable() throws Exception {
        // When & Then
        mvc.perform(get("/api/auth/check-email")
                        .param("email", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.exists").value(true))
                .andDo(print());
    }

    @DisplayName("[통합] 닉네임 중복 확인 - 사용 가능한 닉네임")
    @Test
    void givenAvailableNickname_whenCheckNickname_thenReturnsAvailable() throws Exception {
        // Given
        String availableNickname = "사용가능닉네임";

        // When & Then
        mvc.perform(get("/api/auth/check-nickname")
                        .param("nickname", availableNickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.exists").value(false))
                .andDo(print());
    }
}