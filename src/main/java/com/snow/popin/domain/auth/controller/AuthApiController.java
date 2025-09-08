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

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest req){
        return ResponseEntity.ok(authService.signup(req));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req){
        return ResponseEntity.ok(authService.login(req));
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

    // ================ 중복 확인 API ================
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestParam String email){
        boolean exists = authService.emailExists(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !exists );
        response.put("exists", exists );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean exists = authService.nicknameExists(nickname);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !exists);
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }
}