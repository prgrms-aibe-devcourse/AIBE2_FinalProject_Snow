package com.example.popin.global.jwt;

import com.example.popin.domain.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try{
            String token = resolveToken(request);

            if (token != null && jwtUtil.validateToken(token)){

                log.debug("✅ 토큰 유효함");

                String email = jwtUtil.getEmail(token);

                if (!StringUtils.hasText(email)) {
                    log.warn("❌ 토큰에 email(subject)이 없음 -> invalid token 처리");
                    SecurityContextHolder.clearContext();
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증 실패 : 토큰에 사용자 정보가 없습니다.");
                    return;
                }

                UserDetails userDetails = authService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                          );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("✅ 인증 객체 등록 완료 : {}", email);

            } else {
                log.debug("❌ 토큰이 null이거나 유효하지 않음");
            }

            filterChain.doFilter(request,response);

        } catch (Exception e){
            log.error("❌ 필터에서 예외 발생: {}", e.getMessage());
            // 클라이언트에게는 표준 401 Unauthorized 에러를 반환
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized : Invalid Token");

        }

    }

    private String resolveToken(HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")){
            return bearer.substring(7);
        }

        Cookie[] cookies = request.getCookies();

        if (cookies != null){
            for (Cookie cookie : cookies){
                if ("jwtToken".equals(cookie.getName())){
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){

        String path = request.getRequestURI();

        // 일단 로그인과 회원가입 경로만 필터링 제외
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/signup") ||
                path.equals("/api/auth/check-email") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/static/") ||
                path.equals("/favicon.ico") ||
                path.equals("/") ||
                path.equals("/auth/signup") ||
                path.equals("/error");
    }
}
