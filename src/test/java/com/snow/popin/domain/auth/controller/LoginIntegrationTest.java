package com.snow.popin.domain.auth.controller;


import com.snow.popin.domain.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snow.popin.domain.auth.constant.AuthProvider;
import com.snow.popin.domain.auth.dto.LoginRequest;
import com.snow.popin.domain.user.UserRepository;
import com.snow.popin.domain.user.constant.Role;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.constant.ErrorCode;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("로그인 통합 테스트")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        // 256비트(32바이트) 이상. 예시는 Base64 인코딩된 32바이트 난수입니다.
        "jwt.secret=rwNscFdjVS7RxDUVt8hkPRImnFUay1kmMy34XvurwjY="
})
@Transactional
class LoginIntegrationTest {

    @Autowired
    private MockMvc mvc; // MockMvc는 컨테이너가 주입하게 두면 됩니다.


    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private AuthService authService;


    private User testUser;
    private final String TEST_EMAIL = "integration@example.com";
    private final String TEST_PASSWORD = "testPassword123";


    @BeforeEach
    void setUp() {
// WebApplicationContext나 MockMvcBuilders가 필요 없습니다.
// 실제 MySQL을 바라보는 데이터소스에 사용자 픽스처를 적재합니다.
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

    @DisplayName("[통합] 로그인 성공 - 실제 데이터베이스와 함께 로그인 플로우 테스트")
    @Test
    void givenRealUser_whenLogin_thenReturnsSuccessResponse() throws Exception {
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

    @DisplayName("[통합] 로그인 실패 - 존재하지 않는 사용자")
    @Test
    void givenNonExistentUser_whenLogin_thenReturnsNotFoundError() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("somePassword");

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.LOGIN_FAILED.getCode()))
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @DisplayName("[통합] 로그인 실패 - 잘못된 비밀번호")
    @Test
    void givenWrongPassword_whenLogin_thenReturnsBadRequestError() throws Exception {
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
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.LOGIN_FAILED.getCode()))
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @DisplayName("[통합] 회원가입 후 즉시 로그인 테스트")
    @Test
    void givenNewUser_whenSignupAndLogin_thenBothSucceed() throws Exception {
        // Given - 새로운 사용자 정보
        String newEmail = "newuser@example.com";
        String newPassword = "newPassword123";

        Map<String, String> signupData = new HashMap<>();
        signupData.put("email", newEmail);
        signupData.put("password", newPassword);
        signupData.put("name", "신규가입유저");
        signupData.put("nickname", "신규");
        signupData.put("phone", "010-9999-8888");

        // When 1 - 회원가입
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupData)))
                .andExpect(status().isOk())
                .andDo(print());

        // When 2 - 즉시 로그인 시도
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(newEmail);
        loginRequest.setPassword(newPassword);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.email").value(newEmail))
                .andExpect(jsonPath("$.name").value("신규가입유저"))
                .andDo(print());
    }

    @DisplayName("[통합] 이메일 중복 확인 - 기존 사용자")
    @Test
    void givenExistingEmail_whenCheckEmailDuplicate_thenReturnsExists() throws Exception {
        // When & Then
        mvc.perform(get("/api/auth/check-email")
                        .param("email", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.exists").value(true))
                .andDo(print());
    }

    @DisplayName("[통합] 이메일 중복 확인 - 새로운 이메일")
    @Test
    void givenNewEmail_whenCheckEmailDuplicate_thenReturnsAvailable() throws Exception {
        // Given
        String newEmail = "available@example.com";

        // When & Then
        mvc.perform(get("/api/auth/check-email")
                        .param("email", newEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.exists").value(false))
                .andDo(print());
    }

    @DisplayName("[통합] 중복 이메일로 회원가입 시도")
    @Test
    void givenDuplicateEmail_whenSignup_thenReturnsDuplicateError() throws Exception {
        // Given - 이미 존재하는 이메일로 회원가입 시도
        Map<String, String> signupData = new HashMap<>();
        signupData.put("email", TEST_EMAIL); // 이미 존재하는 이메일
        signupData.put("password", "anotherPassword");
        signupData.put("name", "중복시도유저");
        signupData.put("nickname", "중복");
        signupData.put("phone", "010-3333-4444");

        // When & Then
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.DUPLICATE_EMAIL.getCode()))
                .andDo(print());
    }

    @DisplayName("[통합] 잘못된 JSON 형식으로 로그인 요청")
    @Test
    void givenMalformedJson_whenLogin_thenReturnsBadRequest() throws Exception {
        // Given - 잘못된 JSON
        String malformedJson = "{\"email\":\"test@test.com\",\"password\":}";

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("[통합] 빈 요청 본문으로 로그인 시도")
    @Test
    void givenEmptyBody_whenLogin_thenReturnsBadRequest() throws Exception {
        // When & Then
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("[통합] Content-Type 없이 로그인 요청")
    @Test
    void givenNoContentType_whenLogin_thenReturnsUnsupportedMediaType() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        // When & Then
        mvc.perform(post("/api/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnsupportedMediaType())
                .andDo(print());
    }

    @DisplayName("[통합] 여러 사용자의 동시 로그인 시나리오")
    @Test
    void givenMultipleUsers_whenConcurrentLogin_thenAllSucceed() throws Exception {
        // Given - 추가 사용자들 생성
        User user2 = User.builder()
                .email("user2@example.com")
                .password(passwordEncoder.encode("password2"))
                .name("사용자2")
                .nickname("유저2")
                .phone("010-2222-3333")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        User user3 = User.builder()
                .email("user3@example.com")
                .password(passwordEncoder.encode("password3"))
                .name("사용자3")
                .nickname("유저3")
                .phone("010-4444-5555")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        userRepository.save(user2);
        userRepository.save(user3);

        // When & Then - 첫 번째 사용자 로그인
        LoginRequest request1 = new LoginRequest();
        request1.setEmail(TEST_EMAIL);
        request1.setPassword(TEST_PASSWORD);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));

        // When & Then - 두 번째 사용자 로그인
        LoginRequest request2 = new LoginRequest();
        request2.setEmail("user2@example.com");
        request2.setPassword("password2");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user2@example.com"));

        // When & Then - 세 번째 사용자 로그인
        LoginRequest request3 = new LoginRequest();
        request3.setEmail("user3@example.com");
        request3.setPassword("password3");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user3@example.com"));
    }
}