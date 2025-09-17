package com.snow.popin.domain.chat.controller;

import com.snow.popin.domain.chat.dto.ChatMessageDto;
import com.snow.popin.domain.chat.entity.ChatMessage;
import com.snow.popin.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageDto dto) {
        try {
            // senderId 파라미터 추가
            ChatMessage saved = chatService.saveMessage(
                    dto.getReservationId(),
                    dto.getContent(),
                    dto.getSenderId()
            );

            ChatMessageDto response = new ChatMessageDto(saved);

            messagingTemplate.convertAndSend(
                    "/topic/reservation/" + dto.getReservationId(),
                    response
            );

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "메시지 전송 실패: " + e.getMessage());

            messagingTemplate.convertAndSend(
                    "/topic/reservation/" + dto.getReservationId(),
                    errorResponse
            );
        }
    }
}