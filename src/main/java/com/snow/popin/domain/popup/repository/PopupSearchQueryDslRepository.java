package com.snow.popin.domain.popup.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snow.popin.domain.popup.entity.Popup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.stream.Collectors;

import static com.snow.popin.domain.category.entity.QCategory.category;
import static com.snow.popin.domain.map.entity.QVenue.venue;
import static com.snow.popin.domain.popup.entity.QPopup.popup;
import static com.snow.popin.domain.popup.entity.QTag.tag;

@Repository
@RequiredArgsConstructor
public class PopupSearchQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    /**
     * 팝업 제목과 태그로 검색 - 기존 searchByTitleAndTags 대체
     */
    public Page<Popup> searchByTitleAndTags(String query, Pageable pageable) {
        if (!StringUtils.hasText(query)) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        String lowerQuery = query.toLowerCase().trim();

        List<Popup> content = queryFactory
                .selectDistinct(popup)
                .from(popup)
                .leftJoin(popup.tags, tag)
                .leftJoin(popup.venue, venue).fetchJoin() // N+1 방지
                .leftJoin(popup.category, category).fetchJoin() // N+1 방지
                .where(
                        popup.title.lower().contains(lowerQuery)
                                .or(tag.name.lower().contains(lowerQuery))
                )
                .orderBy(popup.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(popup.countDistinct())
                .from(popup)
                .leftJoin(popup.tags, tag)
                .where(
                        popup.title.lower().contains(lowerQuery)
                                .or(tag.name.lower().contains(lowerQuery))
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 자동완성 검색어 조회 - 기존 findSuggestions 대체
     * Native Query 사용 (성능상 이유로 유지)
     */
    public List<String> findSuggestions(String query, int limit) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        String nativeQuery =
                "(" +
                        "SELECT DISTINCT p.title as suggestion FROM popups p " +
                        "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "UNION " +
                        "SELECT DISTINCT t.name as suggestion FROM popups p " +
                        "JOIN popup_tags pt ON p.id = pt.popup_id " +
                        "JOIN tags t ON pt.tag_id = t.id " +
                        "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))" +
                        ") ORDER BY LENGTH(suggestion), suggestion " +
                        "LIMIT :limit";

        Query nativeQueryObj = entityManager.createNativeQuery(nativeQuery);
        nativeQueryObj.setParameter("query", query.toLowerCase().trim());
        nativeQueryObj.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<String> results = nativeQueryObj.getResultList();

        return results.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}