package com.snow.popin.domain.bookmark.controller;

import com.snow.popin.domain.bookmark.dto.BookMarkListResponseDto;
import com.snow.popin.domain.bookmark.dto.BookMarkRequestDto;
import com.snow.popin.domain.bookmark.dto.BookMarkResponseDto;
import com.snow.popin.domain.bookmark.service.BookMarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookMarkController {

    private final BookMarkService bookMarkService;

    // 북마크 추가
    @PostMapping
    public ResponseEntity<BookMarkResponseDto> addBookmark(
            @RequestHeader("User-Id") Long userId, // TODO: JWT에서 추출하도록 변경
            @Valid @RequestBody BookMarkRequestDto request) {

        BookMarkResponseDto response = bookMarkService.addBookmark(userId, request.getPopupId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 북마크 삭제
    @DeleteMapping("/{popupId}")
    public ResponseEntity<Void> removeBookmark(
            @RequestHeader("User-Id") Long userId, // TODO: JWT에서 추출하도록 변경
            @PathVariable Long popupId) {

        bookMarkService.removeBookmark(userId, popupId);
        return ResponseEntity.noContent().build();
    }

    // 북마크 토글 (있으면 삭제, 없으면 추가)
    @PostMapping("/toggle/{popupId}")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @RequestHeader("User-Id") Long userId, // TODO: JWT에서 추출하도록 변경
            @PathVariable Long popupId) {

        BookMarkResponseDto response = bookMarkService.toggleBookmark(userId, popupId);

        Map<String, Object> result = Map.of(
                "bookmarked", response != null,
                "bookmark", response != null ? response : Map.of()
        );

        return ResponseEntity.ok(result);
    }

    // 사용자 북마크 목록 조회
    @GetMapping
    public ResponseEntity<BookMarkListResponseDto> getUserBookmarks(
            @RequestHeader("User-Id") Long userId, // TODO: JWT에서 추출하도록 변경
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        BookMarkListResponseDto response = bookMarkService.getUserBookmarks(userId, page, size);
        return ResponseEntity.ok(response);
    }

    // 북마크 여부 확인
    @GetMapping("/check/{popupId}")
    public ResponseEntity<Map<String, Boolean>> checkBookmark(
            @RequestHeader("User-Id") Long userId, // TODO: JWT에서 추출하도록 변경
            @PathVariable Long popupId) {

        boolean isBookmarked = bookMarkService.isBookmarked(userId, popupId);
        return ResponseEntity.ok(Map.of("bookmarked", isBookmarked));
    }

    // 사용자별 북마크 수 조회
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUserBookmarkCount(
            @RequestHeader("User-Id") Long userId) { // TODO: JWT에서 추출하도록 변경

        long count = bookMarkService.getUserBookmarkCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // 팝업별 북마크 수 조회
    @GetMapping("/count/popup/{popupId}")
    public ResponseEntity<Map<String, Long>> getPopupBookmarkCount(@PathVariable Long popupId) {
        long count = bookMarkService.getPopupBookmarkCount(popupId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // 사용자가 북마크한 팝업 ID 목록 조회
    @GetMapping("/popup-ids")
    public ResponseEntity<List<Long>> getUserBookmarkedPopupIds(
            @RequestHeader("User-Id") Long userId) { // TODO: JWT에서 추출하도록 변경

        List<Long> popupIds = bookMarkService.getUserBookmarkedPopupIds(userId);
        return ResponseEntity.ok(popupIds);
    }
}