package com.snow.popin.domain.auth.controller;

import com.snow.popin.domain.auth.dto.*;
import com.snow.popin.global.exception.GeneralException;
import com.snow.popin.domain.auth.dto.*;
import com.snow.popin.domain.auth.service.AuthService;
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
    public ResponseEntity<LogoutResponse> logout(
            @RequestBody(required = false) LogoutRequest req,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes
    ) {
        log.info("로그아웃 요청 처리");

        if (req == null){
            req = new LogoutRequest();
        }

        try{
            LogoutResponse res = authService.logout(req, httpReq, httpRes);
            log.info("로그아웃 처리 완료");
            return ResponseEntity.ok(res);
        } catch (Exception e){
            log.error("로그아웃 API 처리 오류 : {}", e.getMessage(), e);
            // 실패해도 성공으로 응답 (클라이언트에서 토큰 정리)
            return ResponseEntity.ok(LogoutResponse.success("로그아웃이 완료되었습니다"));
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