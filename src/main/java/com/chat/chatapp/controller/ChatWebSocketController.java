package com.chat.chatapp.controller;

import com.chat.chatapp.dto.MessageRequest;
import com.chat.chatapp.dto.MessageResponse;
import com.chat.chatapp.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Handles STOMP WebSocket messages from clients.
 *
 * Clients send messages to:  /app/chat.send
 * Server broadcasts to:      /topic/room/{roomId}
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * Client sends:  SEND /app/chat.send
     * Payload:       { "content": "Hello!", "roomId": 1 }
     *
     * JWT principal is automatically set from the WebSocket handshake interceptor.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageRequest request, Principal principal) {
        chatService.sendMessage(request, principal.getName());
    }
}
