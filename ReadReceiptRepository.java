package com.chatapp.repository;

import com.chatapp.model.ReadReceipt;
import com.chatapp.model.PrivateMessage;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReadReceiptRepository extends JpaRepository<ReadReceipt, Long> {
    List<ReadReceipt> findByMessage(PrivateMessage message);
    Optional<ReadReceipt> findByMessageAndReader(PrivateMessage message, User reader);
}