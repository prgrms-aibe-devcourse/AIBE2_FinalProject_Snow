package com.example.popin.global.config;

import com.example.popin.domain.auth.AuthService;
import com.example.popin.global.jwt.JwtFilter;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final AuthService authService;
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

                // HTML 페이지용, 일단 세션 기반으로 만들어둠
                // 추후 필요없으면 삭제
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .defaultSuccessUrl("/",true)
                        .usernameParameter("email")
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID","jwtToken")
                        .permitAll()
                )

                .authorizeRequests(authz -> authz

                        .antMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico").permitAll()

                        // 공개 페이지
                        // TODO : 팝업 리스트, 검색 등 경로 설정되면 추가하기
                        .antMatchers("/", "/index.html", "/main", "/error").permitAll()

                        .antMatchers("/auth/**").permitAll()

                        .antMatchers("/api/auth/**").permitAll()

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
}