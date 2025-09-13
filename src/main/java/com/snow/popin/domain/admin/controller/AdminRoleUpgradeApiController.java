package com.snow.popin.domain.admin.controller;

import com.snow.popin.domain.admin.service.AdminRoleUpgradeService;
import com.snow.popin.domain.roleupgrade.dto.AdminUpdateRequest;
import com.snow.popin.domain.roleupgrade.dto.RoleUpgradeResponse;
import com.snow.popin.domain.roleupgrade.entity.ApprovalStatus;
import com.snow.popin.domain.user.constant.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/role-upgrade")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleUpgradeApiController {

    private final AdminRoleUpgradeService roleService;

    @GetMapping("/requests")
    public ResponseEntity<Page<RoleUpgradeResponse>> getAllRequests(
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<RoleUpgradeResponse> reqs;

        if (status != null && role != null) {
            // 상태와 역할 모두 필터링
            reqs = roleService.getRoleUpgradeRequestsByStatusAndRole(status, role, pageable);
        } else if (status != null) {
            // 상태만 필터링
            reqs = roleService.getRoleUpgradeRequestsByStatus(status, pageable);
        } else if (role != null) {
            // 역할만 필터링
            reqs = roleService.getRoleUpgradeRequestsByRole(role, pageable);
        } else {
            // 전체 조회
            reqs = roleService.getAllRoleUpgradeRequests(pageable);
        }

        return ResponseEntity.ok(reqs);
    }

    // 역할 승격 요청 상세 조회
    @GetMapping("/requests/{id}")
    public ResponseEntity<RoleUpgradeResponse> getRequestDetail(@PathVariable Long id) {
        RoleUpgradeResponse res = roleService.getRoleUpgradeRequestForAdmin(id);
        return ResponseEntity.ok(res);
    }

    // 역할 승격 요청 처리 (승인/거절)
    @PutMapping("/requests/{id}/process")
    public ResponseEntity<Map<String, String>> processRequest(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateRequest req) {
        roleService.processRoleUpgradeRequest(id, req);

        String msg = req.isApprove() ?
                "역할 승격 요청이 승인되었습니다." : "역할 승격 요청이 거절되었습니다.";

        return ResponseEntity.ok(Map.of("message", msg));
    }

    // 대기중인 요청 개수 조회
    @GetMapping("/pending-count")
    public ResponseEntity<Long> getPendingCount(){
        long count = roleService.getPendingRequestCount();
        return ResponseEntity.of(java.util.Optional.of(count));
    }

}