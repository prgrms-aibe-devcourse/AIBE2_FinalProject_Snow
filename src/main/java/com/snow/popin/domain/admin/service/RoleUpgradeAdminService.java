package com.snow.popin.domain.admin.service;

import com.snow.popin.domain.roleupgrade.dto.AdminUpdateRequest;
import com.snow.popin.domain.roleupgrade.dto.RoleUpgradeResponse;
import com.snow.popin.domain.roleupgrade.entity.ApprovalStatus;
import com.snow.popin.domain.roleupgrade.entity.RoleUpgrade;
import com.snow.popin.domain.roleupgrade.repository.RoleUpgradeRepository;
import com.snow.popin.domain.user.constant.Role;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.domain.user.repository.UserRepository;
import com.snow.popin.global.constant.ErrorCode;
import com.snow.popin.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.snow.popin.domain.roleupgrade.service.RoleUpgradeService.validatePendingStatus;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RoleUpgradeAdminService {

    private final RoleUpgradeRepository roleRepo;
    private final UserRepository userRepo;

    // 대기중인 요청 개수 조회 (관리자용)
    public long getPendingRequestCount(){
        return roleRepo.countByStatus(ApprovalStatus.PENDING);
    }

    // 관리자용: 모든 역할 승격 요청 페이징 조회
    public Page<RoleUpgradeResponse> getAllRoleUpgradeRequests(Pageable pageable){
        Page<RoleUpgrade> reqs = roleRepo.findAllByOrderByCreatedAtDesc(pageable);
        return reqs.map(RoleUpgradeResponse::from);
    }

    // 관리자용: 상태별 역할 승격 요청 조회
    public Page<RoleUpgradeResponse> getRoleUpgradeRequestsByStatus(ApprovalStatus status, Pageable pageable){
        Page<RoleUpgrade> reqs = roleRepo.findByStatusOrderByCreatedAtDesc(status, pageable);
        return reqs.map(RoleUpgradeResponse::from);
    }

    // 관리자용: 요청 역할별 역할 승격 요청 조회
    public Page<RoleUpgradeResponse> getRoleUpgradeRequestsByRole(Role role, Pageable pageable){
        Page<RoleUpgrade> reqs = roleRepo.findByRequestedRoleOrderByCreatedAtDesc(role, pageable);
        return reqs.map(RoleUpgradeResponse::from);
    }

    // 관리자용: 상태와 역할 모두로 필터링한 역할 승격 요청 조회
    public Page<RoleUpgradeResponse> getRoleUpgradeRequestsByStatusAndRole(
            ApprovalStatus status, Role role,Pageable pageable ){
        Page<RoleUpgrade> reqs = roleRepo.findByStatusAndRequestedRoleOrderByCreatedAtDesc(status, role, pageable);
        return reqs.map(RoleUpgradeResponse::from);
    }

    // 관리자용: 역할 승격 요청 상세 조회 (권한 체크 없음)
    public RoleUpgradeResponse getRoleUpgradeRequestForAdmin(Long id) {
        RoleUpgrade roleUpgrade = roleRepo.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.ROLE_UPGRADE_REQUEST_NOT_FOUND));

        return RoleUpgradeResponse.from(roleUpgrade);
    }

    // 관리자용: 역할 승격 요청 처리 (승인/반려)
    @Transactional
    public void processRoleUpgradeRequest(Long id, AdminUpdateRequest req){
        RoleUpgrade roleUpgrade = roleRepo.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.ROLE_UPGRADE_REQUEST_NOT_FOUND));

        // 대기중인 요청만 처리 가능
        validatePendingStatus(roleUpgrade);

        if (req.isApprove()){
            // 승인 처리
            roleUpgrade.approve();
            // 사용자 역할 업데이트
            User user = userRepo.findByEmail(roleUpgrade.getEmail())
                    .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

            user.updateRole(roleUpgrade.getRequestedRole());

            log.info("역할 승격이 승인되었습니다. ID: {}, Email: {}, New Role: {}",
                    id, roleUpgrade.getEmail(), roleUpgrade.getRequestedRole());
        } else {
            // 반려처리
            roleUpgrade.reject(req.getRejectReason());

            log.info("역할 승격이 반려되었습니다. ID: {}, Reason: {}", id, req.getRejectReason());
        }
    }

}