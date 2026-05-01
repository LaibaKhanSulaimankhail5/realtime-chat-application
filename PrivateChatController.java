package com.chatapp.controller;

import com.chatapp.dto.PrivateMessageDTO;
import com.chatapp.model.PrivateMessage;
import com.chatapp.service.PrivateMessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PrivateChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PrivateMessageService privateMessageService;

    public PrivateChatController(SimpMessagingTemplate messagingTemplate,
                                 PrivateMessageService privateMessageService) {
        this.messagingTemplate = messagingTemplate;
        this.privateMessageService = privateMessageService;
    }

    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload PrivateMessageDTO messageDTO) {
        privateMessageService.saveMessage(messageDTO);
        messagingTemplate.convertAndSendToUser(
                messageDTO.getReceiverUsername(),
                "/queue/messages",
                messageDTO
        );
    }

    @GetMapping("/api/messages/{user1}/{user2}")
    public List<PrivateMessage> getConversation(
            @PathVariable String user1,
            @PathVariable String user2) {
        return privateMessageService.getConversation(user1, user2);
    }
}