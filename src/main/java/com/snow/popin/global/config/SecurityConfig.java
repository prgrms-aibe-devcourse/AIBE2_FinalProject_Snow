package com.snow.popin.global.config;

import com.snow.popin.global.constant.ErrorCode;
import com.snow.popin.global.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()

                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()

                .authorizeRequests(authz -> authz
                        // 정적 리소스
                        .antMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico").permitAll()

                        // 공개 페이지
                        .antMatchers("/", "/index.html", "/main", "/error").permitAll()

                        // 인증 관련 페이지
                        .antMatchers("/auth/**").permitAll()

                        // 공개 API
                        .antMatchers("/api/auth/login").permitAll()
                        .antMatchers("/api/auth/signup").permitAll()
                        .antMatchers("/api/auth/check-email").permitAll()
                        .antMatchers("/api/auth/logout").permitAll()

                        // 역할별 접근 제어
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .antMatchers("/host/**").hasRole("HOST")
                        .antMatchers("/provider/**").hasRole("PROVIDER")

                        .antMatchers("/api/public/**").permitAll()
                        .antMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // 예외 처리
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((req, res, authException) -> {
                            if (isApiRequest(req)) {
                                sendErrorResponse(res, ErrorCode.UNAUTHORIZED);
                            } else {
                                res.sendRedirect("/auth/login");
                            }
                        })
                        .accessDeniedHandler((req, res, accessDeniedException) -> {
                            if (isApiRequest(req)) {
                                sendErrorResponse(res, ErrorCode.ACCESS_DENIED);
                            } else {
                                res.sendRedirect("/error?code=403");
                            }
                        })
                )

                .headers(headers -> headers
                        .frameOptions().deny()
                        .contentTypeOptions().and()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private boolean isApiRequest(HttpServletRequest req) {
        String reqWith = req.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(reqWith) || req.getRequestURI().startsWith("/api/");
    }
}