package com.snow.popin.global.jwt;

import com.snow.popin.global.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.snow.popin.global.error.ErrorResponseUtil.sendErrorResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtTokenResolver jwtTokenResolver;
    private final ApplicationContext applicationContext;

    // UserDetailsService를 지연 로딩으로 가져오기
    private UserDetailsService getUserDetailsService() {
        return applicationContext.getBean(UserDetailsService.class);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        log.debug("JWT 필터 처리 시작 : {}", requestURI);

        try{
            String token = jwtTokenResolver.resolve(req);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)){
                log.debug("✅ 토큰 유효함");

                String email = jwtUtil.getEmail(token);

                if (StringUtils.hasText(email)){
                    log.debug("토큰에서 이메일 추출 : {}", email);

                    // 이미 인증된 경우 스킵
                    if (SecurityContextHolder.getContext().getAuthentication() == null){
                        try{
                            UserDetails userDetails = getUserDetailsService().loadUserByUsername(email);
                            log.debug("사용자 정보 로드 완료 : {}", userDetails.getUsername());

                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities()
                                    );

                            authenticationToken.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(req)
                            );

                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            log.debug("사용자 인증 설정 완료 : {}",email);

                        } catch (Exception e){
                            log.error("❌ 사용자 정보 로드 실패 : {}", e.getMessage());
                            SecurityContextHolder.clearContext();
                            sendErrorResponse(res, ErrorCode.USER_NOT_FOUND);
                            return;

                        }
                    }
                } else {
                    log.debug("❌ 유효한 토큰을 찾을 수 없습니다.");
                }
            }
            filterChain.doFilter(req, res);
        } catch (Exception e){
            log.error("❌ 필터에서 예외 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            // 클라이언트에게는 표준 401 Unauthorized 에러를 반환
            sendErrorResponse(res, ErrorCode.UNAUTHORIZED);

        }

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        log.debug("필터 제외 경로 확인 : {}", path);

        // 정적 리소스
        if (path.startsWith("/css/") || path.startsWith("/js/") ||
                path.startsWith("/images/") || path.startsWith("/static/") ||
                path.startsWith("/templates/") || path.startsWith("/uploads/") ||
                path.equals("/favicon.ico") || path.endsWith(".json")) {
            return true;
        }

        // 기본 페이지
        if (path.equals("/") || path.equals("/index.html") ||
                path.equals("/main") || path.equals("/error")) {
            return true;
        }

        // 페이지 경로들
        if (path.startsWith("/popup/") || path.equals("/map") ||
                path.startsWith("/users/") || path.startsWith("/missions/")) {
            return true;
        }

        // 인증 페이지
        if (path.startsWith("/auth/")) {
            return true;
        }

        // 공개 API
        if (path.equals("/api/auth/login") ||
                path.equals("/api/auth/signup") ||
                path.equals("/api/auth/check-email") ||
                path.equals("/api/auth/check-nickname") ||
                path.equals("/api/auth/logout")) {
            return true;
        }

        // GET 요청 공개 API들
        String method = req.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            if (path.startsWith("/api/mission-sets/") ||
                    path.startsWith("/api/missions/") ||
                    path.startsWith("/api/popups/") ||
                    path.startsWith("/api/search/") ||
                    path.startsWith("/api/map/")) {
                return true;
            }
        }

        return false;
    }
}