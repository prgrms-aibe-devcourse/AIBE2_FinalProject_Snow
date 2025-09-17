package com.snow.popin.domain.chat.dto;

import com.snow.popin.domain.chat.entity.ChatMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long reservationId;
    private Long senderId;
    private String senderName;
    private String content;
    private String sentAt;

    public ChatMessageDto(ChatMessage message) {
        this.reservationId = message.getReservation().getId();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getNickname();
        this.content = message.getContent();
        this.sentAt = message.getSentAt().toString();
    }
}
