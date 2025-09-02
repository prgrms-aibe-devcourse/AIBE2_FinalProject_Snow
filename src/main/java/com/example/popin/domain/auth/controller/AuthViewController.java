package com.example.popin.domain.auth.controller;

import com.example.popin.domain.auth.AuthService;
import com.example.popin.domain.auth.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;

    @GetMapping("/signup")
    public String signup(){

        return "auth/signup";

    }
}