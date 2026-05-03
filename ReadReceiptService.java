package com.chatapp.service;

import com.chatapp.model.PrivateMessage;
import com.chatapp.model.ReadReceipt;
import com.chatapp.model.User;
import com.chatapp.repository.PrivateMessageRepository;
import com.chatapp.repository.ReadReceiptRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReadReceiptService {

    private final ReadReceiptRepository readReceiptRepository;
    private final PrivateMessageRepository privateMessageRepository;
    private final UserRepository userRepository;

    public ReadReceiptService(ReadReceiptRepository readReceiptRepository,
                              PrivateMessageRepository privateMessageRepository,
                              UserRepository userRepository) {
        this.readReceiptRepository = readReceiptRepository;
        this.privateMessageRepository = privateMessageRepository;
        this.userRepository = userRepository;
    }

    public ReadReceipt markAsRead(Long messageId, String readerUsername) {
        PrivateMessage message = privateMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        User reader = userRepository.findByUsername(readerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check already read nahi hua
        return readReceiptRepository.findByMessageAndReader(message, reader)
                .orElseGet(() -> {
                    ReadReceipt receipt = new ReadReceipt();
                    receipt.setMessage(message);
                    receipt.setReader(reader);
                    return readReceiptRepository.save(receipt);
                });
    }

    public List<ReadReceipt> getReceiptsForMessage(Long messageId) {
        PrivateMessage message = privateMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return readReceiptRepository.findByMessage(message);
    }
}