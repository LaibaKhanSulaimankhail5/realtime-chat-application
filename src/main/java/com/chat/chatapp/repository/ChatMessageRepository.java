package com.chat.chatapp.repository;

import com.chat.chatapp.entity.ChatMessage;
import com.chat.chatapp.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByRoomOrderByTimestampAsc(ChatRoom room, Pageable pageable);
}
