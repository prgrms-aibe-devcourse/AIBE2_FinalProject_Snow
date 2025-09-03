package com.example.popin.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT 유틸리티 테스트")
class JwtUtilTest {

    private JwtUtil sut;
    private final String testSecret = "testsecrettestsecrettestsecrettestsecret";
    private final String testEmail = "test@example.com";
    private final String testName = "테스트유저";
    private final String testRole = "USER";
    private final Long testUserId = 1L;

    @BeforeEach
    void setUp(){
        sut = new JwtUtil(testSecret);
    }

    @DisplayName("유효한 정보로 토큰을 생성하면 JWT 토큰이 반환된다")
    @Test
    void givenValidInfo_whenCreateToken_thenReturnsJwtToken() {
        // When
        String token = sut.createToken(testEmail, testName, testRole);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 3개 부분으로 구성
    }
    @DisplayName("userId를 포함한 토큰을 생성하면 JWT 토큰이 반환된다")
    @Test
    void givenValidInfoWithUserId_whenCreateToken_thenReturnsJwtToken() {
        // When
        String token = sut.createToken(testUserId, testEmail, testName, testRole);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @DisplayName("유효한 토큰에서 이메일을 추출하면 올바른 이메일을 반환한다")
    @Test
    void givenValidToken_whenGetEmail_thenReturnsCorrectEmail() {
        // Given
        String token = sut.createToken(testEmail, testName, testRole);

        // When
        String extractedEmail = sut.getEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo(testEmail);
    }

    @DisplayName("유효한 토큰에서 이름을 추출하면 올바른 이름을 반환한다")
    @Test
    void givenValidToken_whenGetName_thenReturnsCorrectName() {
        // Given
        String token = sut.createToken(testEmail, testName, testRole);

        // When
        String extractedName = sut.getName(token);

        // Then
        assertThat(extractedName).isEqualTo(testName);
    }

    @DisplayName("유효한 토큰에서 역할을 추출하면 올바른 역할을 반환한다")
    @Test
    void givenValidToken_whenGetRole_thenReturnsCorrectRole() {
        // Given
        String token = sut.createToken(testEmail, testName, testRole);

        // When
        String extractedRole = sut.getRole(token);

        // Then
        assertThat(extractedRole).isEqualTo(testRole);
    }

    @DisplayName("잘못된 토큰에서 정보를 추출하려 하면 null을 반환한다")
    @Test
    void givenInvalidToken_whenExtractInfo_thenReturnsNull() {
        // Given
        String invalidToken = "invalid.token.format";

        // When & Then
        assertThat(sut.getEmail(invalidToken)).isNull();
        assertThat(sut.getName(invalidToken)).isNull();
        assertThat(sut.getRole(invalidToken)).isNull();
    }

    @DisplayName("유효한 토큰의 만료 여부를 확인하면 false를 반환한다")
    @Test
    void givenValidToken_whenCheckExpired_thenReturnsFalse() {
        // Given
        String token = sut.createToken(testEmail, testName, testRole);

        // When
        boolean isExpired = sut.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @DisplayName("만료된 토큰의 만료 여부를 확인하면 true를 반환한다")
    @Test
    void givenExpiredToken_whenCheckExpired_thenReturnsTrue() {
        // Given - 만료된 토큰 생성 (과거 시간으로 설정)
        SecretKey secretKey = Keys.hmacShaKeyFor(testSecret.getBytes());
        Date past = new Date(System.currentTimeMillis() - 1000); // 1초 전
        String expiredToken = Jwts.builder()
                .setSubject(testEmail)
                .claim("name", testName)
                .claim("role", testRole)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(past)
                .signWith(secretKey)
                .compact();

        // When
        boolean isExpired = sut.isTokenExpired(expiredToken);

        // Then
        assertThat(isExpired).isTrue();
    }

    @DisplayName("유효한 토큰을 검증하면 true를 반환한다")
    @Test
    void givenValidToken_whenValidate_thenReturnsTrue() {
        // Given
        String token = sut.createToken(testEmail, testName, testRole);

        // When
        boolean isValid = sut.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @DisplayName("잘못된 토큰을 검증하면 false를 반환한다")
    @Test
    void givenInvalidToken_whenValidate_thenReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = sut.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @DisplayName("만료된 토큰을 검증하면 false를 반환한다")
    @Test
    void givenExpiredToken_whenValidate_thenReturnsFalse() {
        // Given - 만료된 토큰 생성
        SecretKey secretKey = Keys.hmacShaKeyFor(testSecret.getBytes());
        Date past = new Date(System.currentTimeMillis() - 1000);
        String expiredToken = Jwts.builder()
                .setSubject(testEmail)
                .setExpiration(past)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        // When
        boolean isValid = sut.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @DisplayName("Claims를 추출하면 올바른 정보가 반환된다")
    @Test
    void givenValidToken_whenExtractClaims_thenReturnsCorrectClaims() {
        // Given
        String token = sut.createToken(testEmail, testName, testRole);

        // When
        Claims claims = sut.extractClaims(token);

        // Then
        assertThat(claims.getSubject()).isEqualTo(testEmail);
        assertThat(claims.get("name", String.class)).isEqualTo(testName);
        assertThat(claims.get("role", String.class)).isEqualTo(testRole);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @DisplayName("빈 문자열이나 null 토큰에 대해 적절히 처리된다")
    @Test
    void givenNullOrEmptyToken_whenProcessToken_thenHandlesGracefully() {
        // When & Then
        assertThat(sut.getEmail(null)).isNull();
        assertThat(sut.getEmail("")).isNull();
        assertThat(sut.getName(null)).isNull();
        assertThat(sut.getRole(null)).isNull();

        assertThat(sut.validateToken(null)).isFalse();
        assertThat(sut.validateToken("")).isFalse();
    }

    @DisplayName("userId가 포함된 토큰에서 userId를 추출할 수 있다 - 현재는 구현되지 않음")
    @Test
    void givenTokenWithUserId_whenGetUserId_thenReturnsNull() {
        // Given - userId가 실제로는 토큰에 저장되지 않음 (createToken 메소드 확인)
        String token = sut.createToken(testUserId, testEmail, testName, testRole);

        // When
        Long extractedUserId = sut.getUserId(token);

        // Then - 현재 구현에서는 userId가 토큰에 저장되지 않아 null 반환
        assertThat(extractedUserId).isNull();
    }
}