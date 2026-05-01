package com.chatapp.controller;

import com.chatapp.dto.PresenceDTO;
import com.chatapp.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

@RestController
@Component
public class PresenceController {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceController(PresenceService presenceService,
                              SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String username = accessor.getUser().getName();
            presenceService.setOnline(username);
            PresenceDTO presence = new PresenceDTO();
            presence.setUsername(username);
            presence.setOnline(true);
            messagingTemplate.convertAndSend("/topic/presence", presence);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String username = accessor.getUser().getName();
            presenceService.setOffline(username);
            PresenceDTO presence = new PresenceDTO();
            presence.setUsername(username);
            presence.setOnline(false);
            messagingTemplate.convertAndSend("/topic/presence", presence);
        }
    }

    @GetMapping("/api/users/online")
    public List<String> getOnlineUsers() {
        return presenceService.getOnlineUsers();
    }
}