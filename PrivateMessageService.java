package com.chatapp.service;

import com.chatapp.dto.PrivateMessageDTO;
import com.chatapp.model.PrivateMessage;
import com.chatapp.model.User;
import com.chatapp.repository.PrivateMessageRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PrivateMessageService {

    private final PrivateMessageRepository privateMessageRepository;
    private final UserRepository userRepository;

    public PrivateMessageService(PrivateMessageRepository privateMessageRepository,
                                 UserRepository userRepository) {
        this.privateMessageRepository = privateMessageRepository;
        this.userRepository = userRepository;
    }

    public PrivateMessage saveMessage(PrivateMessageDTO dto) {
        User sender = userRepository.findByUsername(dto.getSenderUsername())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findByUsername(dto.getReceiverUsername())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        PrivateMessage message = new PrivateMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(dto.getContent());

        return privateMessageRepository.save(message);
    }

    public List<PrivateMessage> getConversation(String user1, String user2) {
        User u1 = userRepository.findByUsername(user1)
                .orElseThrow(() -> new RuntimeException("User not found: " + user1));
        User u2 = userRepository.findByUsername(user2)
                .orElseThrow(() -> new RuntimeException("User not found: " + user2));

        List<PrivateMessage> messages = new ArrayList<>();
        messages.addAll(privateMessageRepository
                .findBySenderAndReceiverOrderBySentAtAsc(u1, u2));
        messages.addAll(privateMessageRepository
                .findByReceiverAndSenderOrderBySentAtAsc(u1, u2));

        messages.sort(Comparator.comparing(PrivateMessage::getSentAt));
        return messages;
    }
}