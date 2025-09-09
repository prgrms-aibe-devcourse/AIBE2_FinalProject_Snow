package com.snow.popin.domain.category.controller;

import com.snow.popin.domain.category.dto.CategoryResponseDto;
import com.snow.popin.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 전체 카테고리 목록 조회
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // TODO: 사용자 관심 카테고리 조회 (로그인 필요)

    // TODO: 사용자 관심 카테고리 업데이트 (로그인 필요)
}