package com.snow.popin.domain.chat.controller;

import com.snow.popin.domain.chat.entity.ChatMessage;
import com.snow.popin.domain.chat.service.ChatService;
import com.snow.popin.domain.spacereservation.entity.SpaceReservation;
import com.snow.popin.domain.spacereservation.repository.SpaceReservationRepository;
import com.snow.popin.domain.space.entity.Space;
import com.snow.popin.domain.popup.entity.Popup;
import com.snow.popin.domain.mypage.host.entity.Brand;
import com.snow.popin.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatApiController {

    private final ChatService chatService;
    private final SpaceReservationRepository reservationRepository;

    @GetMapping("/{reservationId}/messages")
    public ResponseEntity<List<Map<String, Object>>> getMessages(@PathVariable Long reservationId) {
        List<ChatMessage> messages = chatService.getMessages(reservationId);

        List<Map<String, Object>> result = messages.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", m.getId());
                    map.put("reservationId", reservationId);
                    map.put("senderId", m.getSender().getId());
                    map.put("content", m.getContent());
                    map.put("sentAt", m.getSentAt());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{reservationId}/context")
    public ResponseEntity<Map<String, Object>> getChatContext(@PathVariable Long reservationId) {
        try {
            SpaceReservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

            Map<String, Object> context = new HashMap<>();
            context.put("reservationId", reservationId);
            context.put("status", reservation.getStatus().toString());

            // 공간 정보
            Space space = reservation.getSpace();
            if (space != null) {
                context.put("spaceName", space.getTitle());
                context.put("spaceAddress", space.getAddress());
                // 공간 소유자 정보
                if (space.getOwner() != null) {
                    context.put("providerName", space.getOwner().getName());
                }
            }

            // 팝업 정보
            Popup popup = reservation.getPopup();
            if (popup != null) {
                context.put("popupTitle", popup.getTitle());
                context.put("popupSummary", popup.getSummary());
                context.put("popupPeriod", popup.getPeriodText());
            }

            // 브랜드 정보
            Brand brand = reservation.getBrand();
            if (brand != null) {
                context.put("brandName", brand.getName());
                context.put("brandDescription", brand.getDescription());
            }

            // 호스트(예약자) 정보
            User host = reservation.getHost();
            if (host != null) {
                context.put("hostName", host.getName());
                context.put("hostEmail", host.getEmail());
            }

            // 예약 기간
            context.put("startDate", reservation.getStartDate());
            context.put("endDate", reservation.getEndDate());

            // 예약 메시지
            context.put("message", reservation.getMessage());
            context.put("contactPhone", reservation.getContactPhone());

            return ResponseEntity.ok(context);

        } catch (Exception e) {
            System.err.println("채팅 컨텍스트 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}