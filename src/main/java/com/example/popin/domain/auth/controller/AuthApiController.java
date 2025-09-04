package com.example.popin.domain.auth.controller;

import com.example.popin.domain.auth.AuthService;
import com.example.popin.domain.auth.dto.LoginRequest;
import com.example.popin.domain.auth.dto.LoginResponse;
import com.example.popin.domain.auth.dto.SignupRequest;
import com.example.popin.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest req){

        authService.signup(req);

        /* 추후 관심사 추가
        *  if (req.getInterests() != null && !req.getInterests().isEmpty()) {
               userInterestService.saveUserInterests(savedUser.getId(), req.getInterests());
            }
        * */

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req){

        log.info("로그인 시도: {}", req.getEmail());

        try {
            LoginResponse response = authService.login(req);
            log.info("로그인 성공: {}", req.getEmail());
            return ResponseEntity.ok(response);
        } catch (GeneralException e) {
            log.warn("로그인 실패: {} - {}", req.getEmail(), e.getMessage());
            throw e;
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "REFRESH_TOKEN", required = false) String refreshToken,
            HttpServletResponse res
    ){
        // 서버 저장소에서 리프레시 토큰 폐기
        if (refreshToken != null && !refreshToken.isBlank()){
            authService.revokeRe
        }
    }


    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestParam String email){

        boolean exists  = authService.emailExists(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !exists );
        response.put("exists", exists );

        return ResponseEntity.ok(response);

    }

}