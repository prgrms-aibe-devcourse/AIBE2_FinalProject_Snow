package com.snow.popin.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserPageController {

    @GetMapping("/mypage")
    public String myPage() {
        return "forward:/templates/pages/user-mypage.html"; // static/users/mypage.html
    }


    @GetMapping("/user-missions")
    public String myMissions() {
        return "forward:/templates/pages/user-missions.html"; // static/users/user-missions.html
    }
}
