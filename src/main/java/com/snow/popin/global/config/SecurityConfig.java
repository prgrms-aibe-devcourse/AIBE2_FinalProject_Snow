package com.snow.popin.global.config;

import com.snow.popin.domain.auth.service.AuthService;
import com.snow.popin.global.constant.ErrorCode;
import com.snow.popin.global.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;

import static com.snow.popin.global.error.ErrorResponseUtil.sendErrorResponse;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthService authService, JwtFilter jwtFilter) throws Exception {
        http
                .csrf().disable()

                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()

                .authorizeRequests(authz -> authz

                        .antMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico").permitAll()

                        // 공개 페이지
                        // TODO : 팝업 리스트, 검색 등 경로 설정되면 추가하기
                        .antMatchers("/", "/index.html", "/main", "/error").permitAll()

                        // 인증 관련 페이지
                        .antMatchers("/auth/**").permitAll()

                        // 공개 API
                        .antMatchers("/api/auth/login").permitAll()
                        .antMatchers("/api/auth/signup").permitAll()
                        .antMatchers("/api/auth/check-email").permitAll()
                        .antMatchers("/api/auth/logout").permitAll() // 로그아웃은 누구나 접근 가능

                        // TODO : HOST, PROVIDER 경로 설정되면 바꾸기
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .antMatchers("/host/**").hasRole("HOST")
                        .antMatchers("/provider/**").hasRole("PROVIDER")

                        .antMatchers("/api/public/**").permitAll()
                        .antMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )

                .userDetailsService(authService)

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // 예외 처리 추가
                .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((req, res, authException) -> {
                    // 인증되지 않은 사용자
                    if (isApiRequest(req)) {
                        sendErrorResponse(res, ErrorCode.UNAUTHORIZED);
                    } else {
                        res.sendRedirect("/auth/login");
                    }
                })
                .accessDeniedHandler((req, res, accessDeniedException) -> {
                    // 권한 없는 사용자
                    if (isApiRequest(req)) {
                        sendErrorResponse(res, ErrorCode.ACCESS_DENIED);
                    } else {
                        res.sendRedirect("/error?code=403");
                    }
                })
        )

                .headers(headers -> headers
                        .frameOptions().deny() // 클릭재킹 방지
                        .contentTypeOptions().and() // MIME 스니핑 방지
                );


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // API 요청인지 확인
    private boolean isApiRequest(HttpServletRequest req) {

        String reqWith = req.getHeader("X-Request-With");
        return "XMLHttpRequest".equals(reqWith) || req.getRequestURI().startsWith("/api/");

    }
}