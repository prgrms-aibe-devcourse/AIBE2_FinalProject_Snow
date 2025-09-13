package com.snow.popin.domain.roleupgrade.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snow.popin.domain.roleupgrade.dto.*;
import com.snow.popin.domain.roleupgrade.entity.ApprovalStatus;
import com.snow.popin.domain.roleupgrade.entity.DocumentType;
import com.snow.popin.domain.roleupgrade.entity.RoleUpgrade;
import com.snow.popin.domain.roleupgrade.entity.RoleUpgradeDocument;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RoleUpgradeService {

    private final RoleUpgradeRepository roleRepo;
    private final UserRepository userRepo;
    private final ObjectMapper objMapper;


    // 역할 승격 요청 생성
    @Transactional
    public Long createRoleUpgradeRequest(String email, CreateRoleUpgradeRequest  req){

        validateNoDuplicateRequest(email);

        try {
            String payloadJson = objMapper.writeValueAsString(req.getPayload());

            RoleUpgrade roleUpgrade = RoleUpgrade.builder()
                    .email(email)
                    .requestedRole(req.getRequestedRole())
                    .payload(payloadJson)
                    .build();

            RoleUpgrade saved = roleRepo.save(roleUpgrade);
            log.info("역할 승격 요청이 생성되었습니다. ID: {}, Email: {}", saved.getId(), email);

            return saved.getId();

        } catch (Exception e) {
            log.error("역할 승격 요청 생성 실패: {}", e.getMessage());
            throw new GeneralException(ErrorCode.INTERNAL_ERROR);
        }
    }

    // 역할 승격 요청 생성 + file
    @Transactional
    public Long createRoleUpgradeRequestWithDocuments(String email, CreateRoleUpgradeRequest req, List<MultipartFile> files) {

        validateNoDuplicateRequest(email);

        try {
            // 1. 기본 요청 생성
            RoleUpgrade roleUpgrade = RoleUpgrade.builder()
                    .email(email)
                    .requestedRole(req.getRequestedRole())
                    .payload(req.getPayload())
                    .build();

            RoleUpgrade saved = roleRepo.save(roleUpgrade);

            // 2. 파일이 있는 경우에만 문서 처리
            if (files != null && !files.isEmpty()) {
                // 역할에 따른 DocumentType 결정
                DocumentType docType = determineDocTypeByRole(req.getRequestedRole());

                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileUrl = saveFile(file);

                        RoleUpgradeDocument document = RoleUpgradeDocument.builder()
                                .roleUpgrade(saved)
                                .docType(docType)
                                .fileUrl(fileUrl)
                                .build();

                        saved.addDocument(document);
                    }
                }
            }
            log.info("역할 승격 요청 생성 완료. ID: {}, Email: {}, Role: {}, 첨부파일 수: {}",
                    saved.getId(), email, req.getRequestedRole(), files != null ? files.size() : 0);

            return saved.getId();
        } catch (Exception e) {
            log.error("역할 승격 요청 생성 실패: {}", e.getMessage());
            throw new GeneralException(ErrorCode.INTERNAL_ERROR);
        }
    }

    // 역할에 따른 DocumentType 결정
    private DocumentType determineDocTypeByRole(Role requestedRole) {
        switch (requestedRole) {
            case HOST:
                return DocumentType.BUSINESS_LICENSE; // 사업자등록증
            case PROVIDER:
                return DocumentType.REAL_ESTATE;      // 부동산 관련 서류
            default:
                return DocumentType.ETC;              // 기타
        }
    }

    // 내 역할 승격 요청 목록 조회
    public List<RoleUpgradeResponse> getMyRoleUpgradeRequests(String email){
        List<RoleUpgrade> reqs = roleRepo.findByEmailOrderByCreatedAtDesc(email);

        return reqs.stream()
                .map(RoleUpgradeResponse::from)
                .collect(Collectors.toList());
    }

    // 역할 승격 요청 상세 조회
    public RoleUpgradeResponse getRoleUpgradeRequest(Long id, String email){
        RoleUpgrade roleUpgrade = roleRepo.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.ROLE_UPGRADE_REQUEST_NOT_FOUND));

        // 본인의 요청만 조회 (관리자는 별도 메소드 사용)
        if (!roleUpgrade.getEmail().equals(email)){
            throw new GeneralException(ErrorCode.ACCESS_DENIED);
        }
        return RoleUpgradeResponse.from(roleUpgrade);
    }

    // 문서 첨부
    @Transactional
    public void attachDocument(Long upgradeId, String email, DocumentUploadRequest req){
        RoleUpgrade roleUpgrade = findRoleUpgradeById(upgradeId);
        // 본인의 요청인지 확인
        validateOwnership(roleUpgrade, email);
        // 대기중인 요청에만 문서 첨부 가능
        validatePendingStatus(roleUpgrade);

        // 파일 저장 로직 (실제 구현에서는 파일 저장 서비스 사용)
        // String fileUrl = saveFile(request.getFileUrl());
        String fileUrl = "fileUrl";

        RoleUpgradeDocument document = RoleUpgradeDocument.builder()
                .roleUpgrade(roleUpgrade)
                .docType(req.getDocType())
                .businessNumber(req.getBusinessNumber())
                .fileUrl(fileUrl)
                .build();

        roleUpgrade.addDocument(document);

        log.info("문서가 첨부되었습니다. RequestId: {}, DocType: {}", upgradeId, req.getDocType());
    }

    // 공통 로직들
    // 이미 대기중인 요청이 있는지 확인
    private void validateNoDuplicateRequest(String email){
        if (roleRepo.existsByEmailAndStatus(email, ApprovalStatus.PENDING)){
            throw new GeneralException(ErrorCode.DUPLICATE_ROLE_UPGRADE_REQUEST);
        }
    }

    private void validateOwnership(RoleUpgrade roleUpgrade, String email){
        if (!roleUpgrade.getEmail().equals(email)){
            throw new GeneralException(ErrorCode.ACCESS_DENIED);
        }
    }

    // 본인의 요청인지 확인
    private RoleUpgrade findRoleUpgradeById(Long id){
        return roleRepo.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.ROLE_UPGRADE_REQUEST_NOT_FOUND));
    }

    // 대기중인 요청에만 문서 첨부 가능
    public static void validatePendingStatus(RoleUpgrade roleUpgrade) {
        if (!roleUpgrade.isPending()) {
            throw new GeneralException(ErrorCode.INVALID_REQUEST_STATUS);
        }
    }

    // 파일 저장 (실제 구현에서는 파일 저장 서비스로 분리)
    private String saveFile(MultipartFile file){
        try{
            // 실제로는 AWS S3, 로컬 저장소 등을 사용
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = "/uploads/role-upgrade/" + fileName;

            // 파일 저장 로직 구현 필요
            // file.transferTo(new File(filePath));

            return filePath;
        } catch (Exception e) {
            log.error("파일 저장 중 오류 발생: {}", e.getMessage());
            throw new GeneralException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
}