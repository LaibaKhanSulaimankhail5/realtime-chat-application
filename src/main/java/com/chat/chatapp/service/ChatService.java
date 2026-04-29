package com.chat.chatapp.service;

import com.chat.chatapp.dto.*;
import com.chat.chatapp.entity.ChatMessage;
import com.chat.chatapp.entity.ChatRoom;
import com.chat.chatapp.entity.User;
import com.chat.chatapp.repository.ChatMessageRepository;
import com.chat.chatapp.repository.ChatRoomRepository;
import com.chat.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ─── Room Management ────────────────────────────────────────────────────

    @Transactional
    public ChatRoomResponse createRoom(ChatRoomRequest request, String ownerEmail) {
        if (chatRoomRepository.existsByName(request.getName())) {
            throw new RuntimeException("Room name '" + request.getName() + "' already exists.");
        }
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoom room = new ChatRoom();
        room.setName(request.getName());
        room.setDescription(request.getDescription());
        room.setOwner(owner);

        ChatRoom saved = chatRoomRepository.save(room);
        return mapRoomToResponse(saved);
    }

    public List<ChatRoomResponse> getAllRooms() {
        return chatRoomRepository.findAll().stream()
                .map(this::mapRoomToResponse)
                .collect(Collectors.toList());
    }

    public ChatRoomResponse getRoomById(Long id) {
        ChatRoom room = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return mapRoomToResponse(room);
    }

    // ─── Messaging ───────────────────────────────────────────────────────────

    @Transactional
    public MessageResponse sendMessage(MessageRequest request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setRoom(room);

        ChatMessage saved = chatMessageRepository.save(message);
        MessageResponse response = mapMessageToResponse(saved);

        // Broadcast to all subscribers of this room's topic
        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), response);

        return response;
    }

    public Page<MessageResponse> getRoomHistory(Long roomId, int page, int size) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return chatMessageRepository
                .findByRoomOrderByTimestampAsc(room, PageRequest.of(page, size, Sort.by("timestamp").ascending()))
                .map(this::mapMessageToResponse);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ChatRoomResponse mapRoomToResponse(ChatRoom room) {
        return ChatRoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .ownerUsername(room.getOwner() != null ? room.getOwner().getUsername() : "N/A")
                .createdAt(room.getCreatedAt())
                .build();
    }

    private MessageResponse mapMessageToResponse(ChatMessage msg) {
        return MessageResponse.builder()
                .id(msg.getId())
                .senderUsername(msg.getSender().getUsername())
                .content(msg.getContent())
                .roomId(msg.getRoom().getId())
                .roomName(msg.getRoom().getName())
                .timestamp(msg.getTimestamp())
                .build();
    }
}
