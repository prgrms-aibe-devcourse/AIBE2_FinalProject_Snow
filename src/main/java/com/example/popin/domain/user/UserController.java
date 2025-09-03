package com.example.popin.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/new")
    public String userForm(Model model) {
        model.addAttribute("userFormDto", new UserFormDto());
        return "user/userForm";
    }

    @PostMapping("/new")
    public String newUser(@Valid UserFormDto userFormDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "user/userForm";
        }
        try {
            User user = User.createUser(userFormDto, passwordEncoder);
            userService.saveUser(user);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/userForm";
        }
        return "redirect:/";
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



}