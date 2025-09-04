package com.example.popin.domain.auth.controller;

import com.example.popin.domain.auth.AuthService;
import com.example.popin.domain.auth.dto.LoginRequest;
import com.example.popin.domain.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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