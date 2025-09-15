package com.snow.popin.domain.admin.service;

import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.mypage.host.entity.Host;
import com.snow.popin.domain.mypage.host.repository.BrandRepository;
import com.snow.popin.domain.mypage.host.repository.HostRepository;
import com.snow.popin.domain.popup.dto.response.PopupManagementResponse;
import com.snow.popin.domain.popup.dto.response.PopupStatsResponse;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.global.constant.ErrorCode;
import com.snow.popin.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Predicate;
/**
 * 관리자 팝업 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPopupService {

    private final PopupRepository popupRepo;
    private final BrandRepository brandRepo;
    private final HostRepository hostRepo;

    /**
     * 팝업 통게 조회
     */
    public PopupStatsResponse getPopupStats(){
        log.debug("팝업 통계 조회 시작");

        return PopupStatsResponse.builder()
                .completed(popupRepo.count())
                .planning(popupRepo.countByStatus(PopupStatus.PLANNED))
                .ongoing(popupRepo.countByStatus(PopupStatus.ONGOING))
                // TODO 일단은 ENDED로 is_hidden 생기면 고치기
                .completed(popupRepo.countByStatus(PopupStatus.ENDED))
                .build();
    }

    /**
     * 관리자용 팝업 목록 조회 (필터링 및 검색 지원)
     */
    public Page<PopupManagementResponse> getPopupsForAdmin(
            Pageable pageable, PopupStatus status, String category, String keyword){
        log.debug("관리자용 팝업 목록 조회 - 상태: {}, 카테고리: {}, 키워드: {}", status, category, keyword);

        Specification<Popup> spec = createPopupSpecification(status, category, keyword);

        Page<Popup> popups = popupRepo.findAll(spec, pageable);

        return popups.map(PopupManagementResponse::from);
    }

    /**
     * 팝업 관리 정보 조회 (브랜드 + 주최자 정보 포함)
     */
    public PopupManagementResponse getPopupForAdmin(Long popupId){
        Popup popup = popupRepo.findById(popupId)
                .orElseThrow(() -> new GeneralException(ErrorCode.POPUP_NOT_FOUND));

        // 브랜드 정보 조회
        Brand brand = brandRepo.getReferenceById(popup.getBrandId());

        return PopupManagementResponse.fromWithBrand(popup,brand);
    }

    /**
     * 팝업 검색 조건 생성
     */
    private Specification<Popup> createPopupSpecification(PopupStatus status, String category, String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 상태 필터
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 카테고리 필터
            if (StringUtils.hasText(category)) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            // 키워드 검색 (팝업명 또는 주최자명)
            if (StringUtils.hasText(keyword)) {
                String searchKeyword = "%" + keyword.toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), searchKeyword);
                Predicate hostNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("user").get("name")), searchKeyword);

                predicates.add(criteriaBuilder.or(titlePredicate, hostNamePredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
