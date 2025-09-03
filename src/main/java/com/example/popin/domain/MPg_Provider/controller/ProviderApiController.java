package com.example.popin.domain.MPg_Provider.controller;

import com.example.popin.domain.space.entity.Space;
import com.example.popin.domain.MPg_Provider.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/provider")
public class ProviderApiController {

    private final ProviderService service;

    public ProviderApiController(ProviderService service) {
        this.service = service;
    }

    // 내 등록 공간 리스트 (마이페이지 카드용)
    @GetMapping("/spaces")
    public ResponseEntity<List<Space>> mySpaces(Principal principal) {
        String email = principal.getName();     // 로그인 사용자 이메일
        return ResponseEntity.ok(service.findMySpaces(email));
    }
}
