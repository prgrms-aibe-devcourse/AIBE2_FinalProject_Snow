package com.snow.popin.domain.user.controller;

import com.snow.popin.domain.user.service.UserService;
import com.snow.popin.domain.user.dto.UserFormDto;
import com.snow.popin.global.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final UserUtil userUtil;
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
    public ResponseEntity<?> getMyInfo() {
        return ResponseEntity.ok(userUtil.getCurrentUserInfo());
    }

    @GetMapping("/name")
    public ResponseEntity<String> getMyName() {
        return ResponseEntity.ok(userUtil.getCurrentUserName());
    }

    @GetMapping("/mypage")
    public String myPage() {
        return "forward:user/user-mypage";
    }
}