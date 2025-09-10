package com.snow.popin.domain.roleupgrade.dto;

import com.snow.popin.domain.roleupgrade.entity.ApprovalStatus;
import com.snow.popin.domain.roleupgrade.entity.RoleUpgrade;
import com.snow.popin.domain.user.constant.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class RoleUpgradeResponse {

    private Long id;
    private String email;
    private Role requestedRole;
    private ApprovalStatus status;
    private String rejectReason;
    private String payload; // JSON 문자열
    private List<DocumentResponse> documents;


    @Builder
    public RoleUpgradeResponse(Long id, String email, Role requestedRole, ApprovalStatus status,
                               String rejectReason, String payload, List<DocumentResponse> documents) {
        this.id = id;
        this.email = email;
        this.requestedRole = requestedRole;
        this.status = status;
        this.rejectReason = rejectReason;
        this.payload = payload;
        this.documents = documents;
    }


    // RoleUpgrade 엔티티를 Response DTO로 변환
    // 순환 참조 주의
    public static RoleUpgradeResponse from(RoleUpgrade roleUpgrade) {
        List<DocumentResponse> documents = roleUpgrade.getDocuments().stream()
                .map(doc -> DocumentResponse.builder()
                        .id(doc.getId())
                        .docType(doc.getDocType())
                        .businessNumber(doc.getBusinessNumber())
                        .fileUrl(doc.getFileUrl())
                        .build()
                ).collect(Collectors.toList());

        return RoleUpgradeResponse.builder()
                .id(roleUpgrade.getId())
                .email(roleUpgrade.getEmail())
                .requestedRole(roleUpgrade.getRequestedRole())
                .status(roleUpgrade.getStatus())
                .rejectReason(roleUpgrade.getRejectReason())
                .payload(roleUpgrade.getPayload())
                .documents(documents)
                .build();
    }
}