package com.example.popin.domain.auth.controller;

import com.example.popin.domain.auth.AuthService;
import com.example.popin.domain.auth.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest req){

        authService.signup(req);

        /* 추후 관심사 추가
        *  if (req.getInterests() != null && !req.getInterests().isEmpty()) {
               userInterestService.saveUserInterests(savedUser.getId(), req.getInterests());
            }
        * */

        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestParam String email){

        boolean exits = authService.emailExists(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exits", !exits);

        return ResponseEntity.ok(response);

    }
}