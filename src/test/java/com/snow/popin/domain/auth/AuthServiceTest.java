package com.snow.popin.domain.auth;

import com.snow.popin.domain.auth.constant.AuthProvider;
import com.snow.popin.domain.auth.dto.LoginRequest;
import com.snow.popin.domain.auth.dto.LoginResponse;
import com.snow.popin.domain.auth.service.AuthService;
import com.snow.popin.domain.user.UserRepository;
import com.snow.popin.domain.user.constant.Role;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.global.constant.ErrorCode;
import com.snow.popin.global.exception.GeneralException;
import com.snow.popin.global.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@DisplayName("AuthService 로그인 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPw")
                .name("테스트유저")
                .nickname("테스터")
                .phone("010-1234-5678")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("로그인 성공 시 토큰과 사용자 정보를 반환한다")
    void givenValidRequest_whenLogin_thenReturnsLoginResponse() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(mockUser.getEmail());
        request.setPassword("raw-password");

        given(userRepository.findByEmail(eq(mockUser.getEmail())))
                .willReturn(Optional.of(mockUser));

        given(passwordEncoder.matches(eq("raw-password"), eq(mockUser.getPassword())))
                .willReturn(true);

        // 토큰 생성 (id, email, nickname, role 순서)
        given(jwtUtil.createToken(
                eq(mockUser.getId()),
                eq(mockUser.getEmail()),
                eq(mockUser.getName()),
                eq(mockUser.getRole().name())
        )).willReturn("jwt-token");

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(mockUser.getId());
        assertThat(response.getEmail()).isEqualTo(mockUser.getEmail());
        assertThat(response.getName()).isEqualTo(mockUser.getName());
        assertThat(response.getRole()).isEqualTo(mockUser.getRole().name());
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다")
    void givenNonExistingEmail_whenLogin_thenThrowsException() {
        // Given
        LoginRequest request = new LoginRequest();
        given(userRepository.findByEmail(request.getEmail()))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 BAD_REQUEST 예외가 발생한다")
    void givenWrongPassword_whenLogin_thenThrowsException() {
        // Given
        LoginRequest request = new LoginRequest();
        given(userRepository.findByEmail(request.getEmail()))
                .willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches(request.getPassword(), mockUser.getPassword()))
                .willReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.BAD_REQUEST);
    }
}