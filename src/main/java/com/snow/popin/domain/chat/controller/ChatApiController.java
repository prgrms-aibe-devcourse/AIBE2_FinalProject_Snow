package com.snow.popin.domain.chat.controller;

import com.snow.popin.domain.chat.entity.ChatMessage;
import com.snow.popin.domain.chat.service.ChatService;
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
}
