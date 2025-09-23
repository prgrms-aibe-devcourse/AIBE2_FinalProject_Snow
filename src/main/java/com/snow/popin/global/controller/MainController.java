package com.snow.popin.global.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html"; // static 폴더의 index.html로 포워드
    }

    @GetMapping("/main")
    public String main() {
        return "forward:/index.html";
    }

    @GetMapping("/home")
    public String home() {
        return "forward:/home.html"; // static 폴더의 home.html로 포워드
    }
     
}