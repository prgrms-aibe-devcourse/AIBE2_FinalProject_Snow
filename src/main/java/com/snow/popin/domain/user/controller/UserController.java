package com.snow.popin.domain.user.controller;

import com.snow.popin.domain.user.dto.UserResponseDto;
import com.snow.popin.domain.user.service.UserService;
import com.snow.popin.domain.user.dto.UserFormDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/new")
    public String userForm(Model model) {
        model.addAttribute("userFormDto", new UserFormDto());
        return "user/userForm";
    }


    @GetMapping("/login")
    public String loginUser() {
        return "user/userLoginForm";
    }

    @GetMapping("/login/error")
    public String loginError(Model model) {
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해 주세요");
        return "user/userLoginForm";
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build(); // 로그인 안 한 경우
        }

        UserResponseDto userDto = userService.getUserProfile(principal.getName());
        return ResponseEntity.ok(userDto);
    }


}