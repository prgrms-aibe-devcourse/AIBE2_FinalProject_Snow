package com.example.popin.domain.auth;

import com.example.popin.domain.auth.constant.AuthProvider;
import com.example.popin.domain.auth.dto.LoginRequest;
import com.example.popin.domain.auth.dto.LoginResponse;
import com.example.popin.domain.user.*;
import com.example.popin.domain.user.constant.Role;
import com.example.popin.domain.user.entity.User;
import com.example.popin.global.constant.ErrorCode;
import com.example.popin.global.exception.GeneralException;
import com.example.popin.global.jwt.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
        given(userRepository.findByEmail(request.getEmail()))
                .willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches(request.getPassword(), mockUser.getPassword()))
                .willReturn(true);
        given(jwtUtil.createToken(mockUser.getId(), mockUser.getName(),
                mockUser.getName(), mockUser.getRole().name()))
                .willReturn("jwt-token");

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