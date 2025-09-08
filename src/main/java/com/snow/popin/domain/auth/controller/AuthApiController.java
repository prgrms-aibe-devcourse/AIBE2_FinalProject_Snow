package com.snow.popin.domain.auth.controller;

import com.snow.popin.domain.auth.dto.*;
import com.snow.popin.domain.auth.service.AuthService;
import com.snow.popin.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService; // LogoutService 의존성 제거됨

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest req){
        authService.signup(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req){
        log.info("로그인 시도: {}", req.getEmail());

        LoginResponse response = authService.login(req);
        log.info("로그인 성공: {}", req.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @RequestBody(required = false) LogoutRequest req,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes
    ) {
        log.info("로그아웃 요청 처리");

        if (req == null){
            req = new LogoutRequest();
        }

        LogoutResponse response = authService.logout(req, httpReq, httpRes);
        log.info("로그아웃 처리 완료");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestParam String email){
        boolean exists = authService.emailExists(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !exists );
        response.put("exists", exists );

        return ResponseEntity.ok(response);
    }
}