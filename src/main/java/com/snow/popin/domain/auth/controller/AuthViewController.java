package com.snow.popin.domain.auth.controller;

import com.snow.popin.domain.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/auth/login.html";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/auth/login?logout=true";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "forward:/auth/signup.html";
    }

}