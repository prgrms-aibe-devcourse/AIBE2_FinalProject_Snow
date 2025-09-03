package com.example.popin.global.config;

import com.example.popin.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .defaultSuccessUrl("/", true)
                        .usernameParameter("username")
                        .failureUrl("/users/login/error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/users/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .authorizeHttpRequests(authz -> authz
                        // 정적 리소스는 모든 사용자 접근 허용
                        .antMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()

                        // HTML 템플릿 파일들 허용
                        .antMatchers("/templates/**").permitAll()

                        // 공개 페이지들
                        .antMatchers("/", "/index.html", "/main").permitAll()

                        // 사용자 관련 페이지
                        .antMatchers("/users/**", "/error").permitAll()

                        // 각 역할별 접근 권한
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .antMatchers("/host/**").hasRole("HOST")
                        .antMatchers("/provider/**").hasRole("PROVIDER")

                        // API는 인증에 따라 처리 (일부는 public, 일부는 authenticated)
                        .antMatchers("/api/public/**").permitAll()
                        .antMatchers("/api/auth/**").permitAll()
                        .antMatchers("/api/**").authenticated()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .userDetailsService(userService)
                .csrf(csrf -> csrf
                        // API 경로는 CSRF 보호 제외 (REST API용)
                        .ignoringAntMatchers("/api/**")
                        // HTML 컴포넌트 로드도 CSRF 제외
                        .ignoringAntMatchers("/html/**", "/components/**")
                )
                .headers(headers -> headers
                        .frameOptions().deny()
                        .contentTypeOptions().and()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}