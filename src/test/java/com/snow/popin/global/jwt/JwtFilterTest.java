package com.snow.popin.global.jwt;


import com.snow.popin.domain.auth.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;



@DisplayName("JWT 필터 단위 테스트")
@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthService authService;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    private final String testToken = "valid.jwt.token";
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Authorization 헤더에 유효한 토큰이 있으면 인증이 설정된다")
    void givenValidTokenInHeader_whenFilter_thenSetsAuthentication() throws Exception {
        // Given
        request.addHeader("Authorization", "Bearer " + testToken);

        given(jwtUtil.validateToken(testToken)).willReturn(true);
        given(jwtUtil.getEmail(testToken)).willReturn(testEmail);

        // 실제 UserDetails 객체 사용
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(testEmail)
                .password("pw")
                .authorities("ROLE_USER")
                .build();
        given(authService.loadUserByUsername(testEmail)).willReturn(userDetails);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(testEmail);
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }
}