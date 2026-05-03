package com.chatapp.controller;

import com.chatapp.model.ReadReceipt;
import com.chatapp.service.ReadReceiptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receipts")
public class ReadReceiptController {

    private final ReadReceiptService readReceiptService;

    public ReadReceiptController(ReadReceiptService readReceiptService) {
        this.readReceiptService = readReceiptService;
    }

    @PostMapping("/read/{messageId}/{username}")
    public ReadReceipt markAsRead(@PathVariable Long messageId,
                                  @PathVariable String username) {
        return readReceiptService.markAsRead(messageId, username);
    }

    @GetMapping("/{messageId}")
    public List<ReadReceipt> getReceipts(@PathVariable Long messageId) {
        return readReceiptService.getReceiptsForMessage(messageId);
    }
}