package com.chat.chatapp.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private Long id;
    private String senderUsername;
    private String content;
    private Long roomId;
    private String roomName;
    private LocalDateTime timestamp;
}
