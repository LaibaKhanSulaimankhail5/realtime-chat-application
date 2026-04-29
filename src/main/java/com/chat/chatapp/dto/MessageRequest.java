package com.chat.chatapp.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String content;
    private Long roomId;
}
