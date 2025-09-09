package com.snow.popin.domain.category.service;

import com.snow.popin.domain.category.dto.CategoryResponseDto;
import com.snow.popin.domain.category.entity.Category;
import com.snow.popin.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    // TODO: UserService 추가 필요 (사용자 관심 카테고리 관리)

    // 전체 카테고리 목록 조회
    public List<CategoryResponseDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAllOrderById();
        return categories.stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());
    }

}
