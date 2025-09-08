package com.snow.popin.domain.auth.controller;

import com.snow.popin.domain.auth.dto.LoginRequest;
import com.snow.popin.domain.auth.dto.LogoutRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("인증 요청 유효성 검증 테스트 (로그인/로그아웃)")
class AuthValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ================ 로그인 검증 테스트 ================

    @DisplayName("유효한 로그인 요청 - 검증 통과")
    @ParameterizedTest(name = "[{index}] email: {0}, password: {1}")
    @MethodSource("validLoginRequests")
    void givenValidLoginRequest_whenValidate_thenNoViolations(String email, String password) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> validLoginRequests() {
        return Stream.of(
                arguments("test@example.com", "password123"),
                arguments("user@domain.co.kr", "mypassword"),
                arguments("admin@company.org", "securePassword!"),
                arguments("a@b.co", "123456"),
                arguments("very.long.email.address@very.long.domain.name.com", "p"),
                arguments("test+tag@example.com", "password with spaces"),
                arguments("123@456.com", "한글비밀번호도가능")
        );
    }

    @DisplayName("이메일 필드 검증 실패 - null, empty, blank")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void givenInvalidEmail_whenValidate_thenHasEmailViolation(String email) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("validPassword");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("email") &&
                                violation.getMessage().contains("필수"));
    }

    @DisplayName("이메일 형식 검증 실패")
    @ParameterizedTest(name = "[{index}] invalid email: {0}")
    @ValueSource(strings = {
            "notanemail",
            "@example.com",
            "test@",
            "test@@example.com",
            "test@.com",
            "test@example.",
            "test space@example.com",
            "test@exam ple.com"
    })
    void givenInvalidEmailFormat_whenValidate_thenHasEmailFormatViolation(String invalidEmail) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(invalidEmail);
        request.setPassword("validPassword");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("email") &&
                                violation.getMessage().contains("형식"));
    }

    @DisplayName("비밀번호 필드 검증 실패 - null, empty, blank")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void givenInvalidPassword_whenValidate_thenHasPasswordViolation(String password) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword(password);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("password") &&
                                violation.getMessage().contains("필수"));
    }

    @DisplayName("이메일과 비밀번호 모두 누락 - 다중 검증 실패")
    @ParameterizedTest(name = "[{index}] email: {0}, password: {1}")
    @MethodSource("invalidLoginRequests")
    void givenBothFieldsInvalid_whenValidate_thenHasMultipleViolations(String email, String password, int expectedViolationCount) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(expectedViolationCount);
    }

    static Stream<Arguments> invalidLoginRequests() {
        return Stream.of(
                arguments(null, null, 2), // 이메일 null, 비밀번호 null
                arguments("", "", 2), // 이메일 empty, 비밀번호 empty
                arguments("   ", "   ", 2), // 이메일 blank, 비밀번호 blank
                arguments("invalid-email", "", 2), // 이메일 형식 오류, 비밀번호 empty
                arguments("test@example.com", null, 1), // 유효한 이메일, 비밀번호 null
                arguments(null, "validPassword", 1) // 이메일 null, 유효한 비밀번호
        );
    }

    @DisplayName("특수 문자가 포함된 유효한 이메일")
    @ParameterizedTest(name = "[{index}] email: {0}")
    @ValueSource(strings = {
            "test.email@example.com",
            "test+tag@example.com",
            "test_underscore@example.com",
            "test-hyphen@example.com",
            "123numbers@example.com",
            "MixedCase@Example.Com"
    })
    void givenValidEmailWithSpecialCharacters_whenValidate_thenNoViolations(String email) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("validPassword");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @DisplayName("비밀번호에 특수문자/공백 포함 - 모두 허용되어야 함")
    @ParameterizedTest(name = "[{index}] password: {0}")
    @ValueSource(strings = {
            "password123",
            "PASSWORD",
            "123456",
            "special!@#$%^&*()",
            "password with spaces",
            "한글비밀번호",
            "p" // 1자리
    })
    void givenVariousValidPasswords_whenValidate_thenNoViolations(String password) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword(password);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @DisplayName("전체 필드 null 체크")
    @Test
    void givenAllFieldsNull_whenValidate_thenHasAllViolations() {
        // Given
        LoginRequest request = new LoginRequest();
        // 모든 필드를 기본값(null)으로 두기

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2); // email, password 각각 1개씩
        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("email", "password");
    }

    @DisplayName("공백 문자열만으로 구성된 필드들")
    @ParameterizedTest(name = "[{index}] whitespace string: '{0}'")
    @ValueSource(strings = {" ", "  ", "\t", "\n", "\r\n", " \t \n "})
    void givenWhitespaceFields_whenValidate_thenHasViolations(String whitespace) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(whitespace);
        request.setPassword(whitespace);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(3); // 이메일 @NotBlank, 이메일 @Email, 비밀번호 @NotBlank = 3개

        // 구체적인 검증
        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString() + ":" + violation.getMessage())
                .containsExactlyInAnyOrder(
                        "email:이메일은 필수입니다.",
                        "email:올바른 이메일 형식이 아닙니다.",
                        "password:비밀번호는 필수입니다."
                );
    }

    @DisplayName("이메일만 유효하고 비밀번호가 무효한 경우")
    @ParameterizedTest(name = "[{index}] valid email with invalid password: '{1}'")
    @MethodSource("validEmailInvalidPasswordCases")
    void givenValidEmailAndInvalidPassword_whenValidate_thenHasPasswordViolation(String validEmail, String invalidPassword) {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(validEmail);
        request.setPassword(invalidPassword);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("password");
    }

    static Stream<Arguments> validEmailInvalidPasswordCases() {
        return Stream.of(
                arguments("test@example.com", null),
                arguments("user@domain.co.kr", ""),
                arguments("admin@company.org", "   "),
                arguments("valid@email.com", "\t"),
                arguments("another@valid.email.com", "\n")
        );
    }

    // ================ 로그아웃 검증 테스트 ================

    @DisplayName("LogoutRequest 유효성 검사 - 모든 필드가 optional이므로 항상 유효")
    @Test
    void givenEmptyLogoutRequest_whenValidate_thenNoViolations() {
        // Given
        LogoutRequest request = new LogoutRequest();

        // When
        Set<ConstraintViolation<LogoutRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @DisplayName("LogoutRequest 다양한 입력값 검증")
    @ParameterizedTest
    @MethodSource("logoutRequestTestCases")
    void givenLogoutRequest_whenValidate_thenNoViolations(LogoutRequest request) {
        // When
        Set<ConstraintViolation<LogoutRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    static Stream<LogoutRequest> logoutRequestTestCases() {
        return Stream.of(
                new LogoutRequest(), // 빈 요청
                new LogoutRequest("access-token", null), // accessToken만
                new LogoutRequest(null, "refresh-token"), // refreshToken만
                new LogoutRequest("access-token", "refresh-token") // 둘 다
        );
    }
}