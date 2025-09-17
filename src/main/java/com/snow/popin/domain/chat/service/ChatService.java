package com.snow.popin.domain.chat.service;

import com.snow.popin.domain.chat.entity.ChatMessage;
import com.snow.popin.domain.chat.repository.ChatMessageRepository;
import com.snow.popin.domain.spacereservation.entity.ReservationStatus;
import com.snow.popin.domain.spacereservation.entity.SpaceReservation;
import com.snow.popin.domain.spacereservation.repository.SpaceReservationRepository;
import com.snow.popin.domain.user.entity.User;
import com.snow.popin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SpaceReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessage saveMessage(Long reservationId, String content, Long senderId) {
        SpaceReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // senderId로 직접 사용자 조회
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        System.out.println("발신자: " + sender.getEmail() + " (ID: " + senderId + ")");

        if (reservation.getStatus() == ReservationStatus.REJECTED ||
                reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이 예약 상태에서는 채팅할 수 없습니다.");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .reservation(reservation)
                .sender(sender)
                .content(content)
                .sentAt(LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);
        System.out.println("저장 완료: " + saved.getContent());
        return saved;
    }

    public List<ChatMessage> getMessages(Long reservationId) {
        return chatMessageRepository.findByReservationIdOrderBySentAtAsc(reservationId);
    }
}