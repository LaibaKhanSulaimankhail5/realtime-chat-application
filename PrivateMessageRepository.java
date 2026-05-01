package com.chatapp.repository;

import com.chatapp.model.PrivateMessage;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
    List<PrivateMessage> findBySenderAndReceiverOrderBySentAtAsc(User sender, User receiver);
    List<PrivateMessage> findByReceiverAndSenderOrderBySentAtAsc(User receiver, User sender);
}