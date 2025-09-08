package com.snow.popin.domain.auth.controller;

import com.snow.popin.domain.auth.dto.LoginRequest;
import com.snow.popin.domain.auth.dto.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("인증 요청 유효성 검증 테스트")
class AuthValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ================ 회원가입 검증 테스트 ================

    @DisplayName("유효한 회원가입 요청 - 검증 통과")
    @Test
    void givenValidSignupRequest_whenValidate_thenNoViolations() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setName("테스트유저");
        request.setNickname("테스터");
        request.setPhone("010-1234-5678");

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @DisplayName("회원가입 - 필수 필드가 null이거나 빈 값일 때 검증 실패")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void givenInvalidSignupFields_whenValidate_thenHasViolations(String invalidValue) {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail(invalidValue); // 필수 필드에 유효하지 않은 값

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @DisplayName("회원가입 - 잘못된 이메일 형식일 때 검증 실패")
    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "test@", "@example.com", "test.example.com"})
    void givenInvalidEmailFormat_whenSignupValidate_thenHasViolations(String invalidEmail) {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail(invalidEmail);
        request.setPassword("password123");
        request.setName("테스트유저");
        request.setNickname("테스터");
        request.setPhone("010-1234-5678");

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    // ================ 로그인 검증 테스트 ================

    @DisplayName("유효한 로그인 요청 - 검증 통과")
    @Test
    void givenValidLoginRequest_whenValidate_thenNoViolations() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @DisplayName("로그인 - 이메일이 null이거나 빈 값일 때 검증 실패")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void givenInvalidEmail_whenLoginValidate_thenHasViolations(String invalidEmail) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(invalidEmail);
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @DisplayName("로그인 - 비밀번호가 null이거나 빈 값일 때 검증 실패")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void givenInvalidPassword_whenLoginValidate_thenHasViolations(String invalidPassword) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword(invalidPassword);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @DisplayName("로그인 - 잘못된 이메일 형식일 때 검증 실패")
    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "test@", "@example.com"})
    void givenInvalidEmailFormat_whenLoginValidate_thenHasViolations(String invalidEmail) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(invalidEmail);
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }
}