package com.chat.chatapp.controller;

import com.chat.chatapp.dto.ChatRoomRequest;
import com.chat.chatapp.dto.ChatRoomResponse;
import com.chat.chatapp.dto.MessageResponse;
import com.chat.chatapp.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for room management and message history.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    // POST /api/chat/rooms  → create a new room
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(
            @RequestBody ChatRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.createRoom(request, userDetails.getUsername()));
    }

    // GET /api/chat/rooms  → list all rooms
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getAllRooms() {
        return ResponseEntity.ok(chatService.getAllRooms());
    }

    // GET /api/chat/rooms/{id}  → single room info
    @GetMapping("/rooms/{id}")
    public ResponseEntity<ChatRoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getRoomById(id));
    }

    // GET /api/chat/rooms/{id}/history?page=0&size=20  → paginated message history
    @GetMapping("/rooms/{id}/history")
    public ResponseEntity<Page<MessageResponse>> getRoomHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getRoomHistory(id, page, size));
    }
}
