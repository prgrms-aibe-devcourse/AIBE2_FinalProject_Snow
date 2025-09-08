package com.snow.popin.domain.auth.service;

import com.snow.popin.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final JwtUtil jwtUtil;

    // 현재는 단순히 클라이언트에서 토큰 제거
    // 향후 블랙리스트 관리나 Redis 활용

    public void processLogout(String token, HttpServletRequest req, HttpServletResponse res){
        try{
            // 토큰 검증
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)){

                String userEmail = jwtUtil.getEmail(token);
                log.info("사용자 로그아웃 처리 : {}", userEmail);

            }

            clearJwtCookie(res);
        } catch (Exception e){
            log.warn("로그아웃 처리 중 오류 발생 (계속 진행) : {}", e.getMessage());
            // 실패해도 클라이언트에서 토큰 제거
        }
    }

    private void clearJwtCookie(HttpServletResponse res) {

        Cookie jwtCookie = new Cookie("jwtToken", "");
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // 개발환경에서는 false, 프로덕션에서는 true
        res.addCookie(jwtCookie);

    }



}
