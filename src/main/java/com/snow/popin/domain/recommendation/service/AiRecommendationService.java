package com.snow.popin.domain.recommendation.service;

import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.mypage.host.repository.BrandRepository;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.popup.entity.PopupStatus;
import com.snow.popin.domain.popup.repository.PopupRepository;
import com.snow.popin.domain.popupReservation.entity.Reservation;
import com.snow.popin.domain.popupReservation.repository.ReservationRepository;
import com.snow.popin.domain.recommendation.dto.AiRecommendationResponseDto;
import com.snow.popin.domain.recommendation.dto.ReservationHistoryDto;
import com.snow.popin.domain.recommendation.dto.UserPreferenceDto;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiRecommendationService {

    private final GeminiAiService geminiAiService;
    private final PopupRepository popupRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;

    // 카테고리 매핑
    private static final Map<Long, String> CATEGORY_MAP = Map.of(
            1L, "패션",
            2L, "반려동물",
            3L, "게임",
            4L, "캐릭터/IP",
            5L, "문화/컨텐츠",
            6L, "연예",
            7L, "여행/레저/스포츠"
    );

    /**
     * 사용자 기반 AI 팝업 추천
     */
    public AiRecommendationResponseDto getPersonalizedRecommendations(Long userId, int limit) {
        try {
            log.info("사용자 {} 에 대한 AI 추천 시작", userId);

            // 1. 사용자 선호도 분석
            UserPreferenceDto userPreference = analyzeUserPreferences(userId);

            // 2. 현재 진행중인 팝업 목록 조회
            List<Popup> availablePopups = popupRepository.findByStatus(PopupStatus.ONGOING);

            // 3. AI 프롬프트 생성
            String prompt = createRecommendationPrompt(userPreference, availablePopups, limit);

            // 4. Gemini API 호출
            String aiResponse = geminiAiService.generateText(prompt);

            if (aiResponse == null) {
                log.warn("AI 응답이 null입니다. 인기 팝업으로 대체합니다.");
                return getPopularPopupsAsFallback(limit);
            }

            // 5. AI 응답 파싱
            return parseAiResponse(aiResponse, availablePopups);

        } catch (Exception e) {
            log.error("AI 추천 처리 중 오류 발생", e);
            return getPopularPopupsAsFallback(limit);
        }
    }

    /**
     * 사용자 선호도 분석
     */
    private UserPreferenceDto analyzeUserPreferences(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return UserPreferenceDto.builder().userId(userId).build();
        }

        // 관심 카테고리 조회
        List<String> interests = getUserInterests(user);

        // 예약 이력 조회 (최근 10개)
        List<ReservationHistoryDto> reservationHistory = getReservationHistory(user);

        return UserPreferenceDto.builder()
                .userId(userId)
                .interests(interests)
                .reservationHistory(reservationHistory)
                .build();
    }

    /**
     * 사용자 관심사 조회
     */
    private List<String> getUserInterests(User user) {
        if (user.getInterests() == null || user.getInterests().isEmpty()) {
            return new ArrayList<>();
        }

        return user.getInterestCategoryNames();
    }

    /**
     * 예약 이력 조회 (최근 10개)
     */
    private List<ReservationHistoryDto> getReservationHistory(User user) {
        List<Reservation> reservations = reservationRepository.findByUser(user);

        return reservations.stream()
                .sorted((r1, r2) -> r2.getReservationDate().compareTo(r1.getReservationDate()))
                .limit(10)
                .map(this::convertToReservationHistoryDto)
                .collect(Collectors.toList());
    }

    /**
     * 예약 이력 DTO 변환
     */
    private ReservationHistoryDto convertToReservationHistoryDto(Reservation reservation) {
        Popup popup = reservation.getPopup();

        // 카테고리 이름 조회
        String categoryName = null;
        if (popup.getCategory() != null && popup.getCategory().getId() != null) {
            categoryName = CATEGORY_MAP.get(popup.getCategory().getId());
        }

        // 브랜드 이름 조회
        String brandName = getBrandName(popup.getBrandId());

        return ReservationHistoryDto.builder()
                .popupId(popup.getId())
                .popupTitle(popup.getTitle())
                .category(categoryName)
                .brand(brandName)
                .reservationDate(reservation.getReservationDate().toLocalDate())
                .status(reservation.getStatus().name())
                .venue(popup.getVenue() != null ? popup.getVenue().getName() : "")
                .build();
    }

    /**
     * 브랜드 이름 조회
     */
    private String getBrandName(Long brandId) {
        if (brandId == null) {
            return "브랜드";
        }

        try {
            return brandRepository.findById(brandId)
                    .map(Brand::getName)
                    .orElse("브랜드");
        } catch (Exception e) {
            log.warn("브랜드 조회 실패 - brandId: {}", brandId, e);
            return "브랜드";
        }
    }

    /**
     * AI 추천 프롬프트 생성
     */
    private String createRecommendationPrompt(UserPreferenceDto userPreference,
                                              List<Popup> availablePopups, int limit) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 팝업스토어 추천 전문가입니다. 사용자의 선호도를 분석하여 가장 적합한 팝업스토어를 추천해주세요.\n\n");

        // 사용자 정보
        prompt.append("## 사용자 정보\n");
        if (userPreference.getInterests() != null && !userPreference.getInterests().isEmpty()) {
            prompt.append("관심 카테고리: ").append(String.join(", ", userPreference.getInterests())).append("\n");
        }

        // 예약 이력
        if (userPreference.getReservationHistory() != null && !userPreference.getReservationHistory().isEmpty()) {
            prompt.append("최근 예약 이력:\n");
            for (ReservationHistoryDto history : userPreference.getReservationHistory()) {
                prompt.append("- ").append(history.getPopupTitle())
                        .append(" (").append(history.getCategory()).append(")")
                        .append(" - ").append(history.getBrand()).append("\n");
            }
        }

        // 현재 진행중인 팝업들 (브랜드 정보 배치 조회로 성능 최적화)
        Map<Long, String> brandMap = getBrandNamesMap(availablePopups);

        prompt.append("\n## 현재 진행중인 팝업 목록\n");
        for (Popup popup : availablePopups) {
            String category = "기타";
            if (popup.getCategory() != null && popup.getCategory().getId() != null) {
                category = CATEGORY_MAP.getOrDefault(popup.getCategory().getId(), "기타");
            }

            String brandName = brandMap.getOrDefault(popup.getBrandId(), "브랜드");

            prompt.append(popup.getId()).append(": ")
                    .append(popup.getTitle()).append(" - ")
                    .append(category).append(" - ")
                    .append(brandName).append("\n");
        }

        // 요청사항
        prompt.append("\n## 요청사항\n");
        prompt.append("위 정보를 바탕으로 사용자에게 가장 적합한 팝업 ").append(limit).append("개를 추천해주세요.\n");
        prompt.append("응답 형식:\n");
        prompt.append("추천 팝업 ID: [1,2,3]\n");
        prompt.append("추천 이유: 사용자의 관심사와 예약 패턴을 고려한 상세한 추천 이유\n");

        return prompt.toString();
    }

    /**
     * 브랜드 이름들을 배치로 조회 (성능 최적화)
     */
    private Map<Long, String> getBrandNamesMap(List<Popup> popups) {
        Set<Long> brandIds = popups.stream()
                .map(Popup::getBrandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (brandIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            List<Brand> brands = brandRepository.findAllById(brandIds);
            return brands.stream()
                    .collect(Collectors.toMap(
                            Brand::getId,
                            Brand::getName,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            log.warn("브랜드 배치 조회 실패", e);
            return new HashMap<>();
        }
    }

    /**
     * AI 응답 파싱
     */
    private AiRecommendationResponseDto parseAiResponse(String aiResponse, List<Popup> availablePopups) {
        try {
            log.info("AI 응답 파싱 시작: {}", aiResponse);

            // 팝업 ID 추출 (정규식 사용)
            Pattern pattern = Pattern.compile("\\[(\\d+(?:,\\s*\\d+)*)\\]");
            Matcher matcher = pattern.matcher(aiResponse);

            List<Long> recommendedIds = new ArrayList<>();
            if (matcher.find()) {
                String idsStr = matcher.group(1);
                String[] ids = idsStr.split(",");
                for (String id : ids) {
                    try {
                        Long popupId = Long.parseLong(id.trim());
                        // 유효한 팝업 ID인지 확인
                        if (availablePopups.stream().anyMatch(p -> p.getId().equals(popupId))) {
                            recommendedIds.add(popupId);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 팝업 ID 형식: {}", id);
                    }
                }
            }

            // 추천 이유 추출
            String reasoning = extractReasoning(aiResponse);

            if (recommendedIds.isEmpty()) {
                log.warn("AI에서 추천한 팝업 ID가 없습니다. 인기 팝업으로 대체합니다.");
                return getPopularPopupsAsFallback(5);
            }

            return AiRecommendationResponseDto.success(recommendedIds, reasoning);

        } catch (Exception e) {
            log.error("AI 응답 파싱 중 오류 발생", e);
            return getPopularPopupsAsFallback(5);
        }
    }

    /**
     * 추천 이유 추출
     */
    private String extractReasoning(String aiResponse) {
        try {
            String[] lines = aiResponse.split("\n");
            for (String line : lines) {
                if (line.contains("추천 이유") || line.contains("이유")) {
                    return line.replaceFirst(".*추천 이유:?\\s*", "").trim();
                }
            }
            return "AI 분석을 통한 개인화 추천입니다.";
        } catch (Exception e) {
            return "AI 분석을 통한 개인화 추천입니다.";
        }
    }

    /**
     * 대체 추천 (인기 팝업)
     */
    private AiRecommendationResponseDto getPopularPopupsAsFallback(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            var popularPopups = popupRepository.findPopularActivePopups(pageable);

            List<Long> popupIds = popularPopups.getContent().stream()
                    .map(Popup::getId)
                    .collect(Collectors.toList());

            return AiRecommendationResponseDto.success(
                    popupIds,
                    "현재 인기있는 팝업들을 추천드립니다."
            );
        } catch (Exception e) {
            log.error("인기 팝업 조회 실패", e);
            return AiRecommendationResponseDto.failure("추천 팝업을 찾을 수 없습니다.");
        }
    }
}