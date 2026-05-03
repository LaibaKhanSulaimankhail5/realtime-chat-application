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
        List<ChatRoom> rooms = chatRoomRepository.findAll();
        if (rooms.isEmpty()) {
            // Create some default rooms if none exist
            createDefaultRoom("Public", "General discussion for everyone");
            createDefaultRoom("Friends", "A place for close friends to chat");
            rooms = chatRoomRepository.findAll();
        }
        return rooms.stream()
                .map(this::mapRoomToResponse)
                .collect(Collectors.toList());
    }

    private void createDefaultRoom(String name, String desc) {
        ChatRoom room = new ChatRoom();
        room.setName(name);
        room.setDescription(desc);
        chatRoomRepository.save(room);
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

    @Transactional
    public MessageResponse editMessage(Long messageId, String newContent, String userEmail) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to edit this message");
        }

        message.setContent(newContent);
        message.setEdited(true);
        ChatMessage saved = chatMessageRepository.save(message);
        
        MessageResponse response = mapMessageToResponse(saved);
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoom().getId(), response);
        return response;
    }

    @Transactional
    public MessageResponse deleteMessage(Long messageId, String userEmail) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to delete this message");
        }

        message.setDeleted(true);
        message.setContent("This message was deleted");
        ChatMessage saved = chatMessageRepository.save(message);

        MessageResponse response = mapMessageToResponse(saved);
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoom().getId(), response);
        return response;
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
                .edited(msg.isEdited())
                .deleted(msg.isDeleted())
                .build();
    }
}
