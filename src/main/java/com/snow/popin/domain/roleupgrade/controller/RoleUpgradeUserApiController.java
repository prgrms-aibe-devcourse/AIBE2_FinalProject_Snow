package com.snow.popin.domain.roleupgrade.controller;

import com.snow.popin.domain.roleupgrade.dto.CreateRoleUpgradeRequest;
import com.snow.popin.domain.roleupgrade.service.RoleUpgradeService;
import com.snow.popin.global.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role-upgrade")
@RequiredArgsConstructor
public class RoleUpgradeUserApiController {

    private final RoleUpgradeService roleService;
    private final UserUtil userUtil;

    @PostMapping("/request")
    public ResponseEntity<?> createRequest(
            @Valid @RequestPart("request") CreateRoleUpgradeRequest req,
            @RequestPart(value = "documents", required = false) List<MultipartFile> files
    ) {

        String userEmail = userUtil.getCurrentUserEmail();
        Long reqId = roleService.createRoleUpgradeRequest(userEmail, req);

        return ResponseEntity.ok(Map.of(
                "message", "역할 승격 요청이 성공적으로 제출되었습니다.",
                "requestId", reqId
        ));
    }
}
