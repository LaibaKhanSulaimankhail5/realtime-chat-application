package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "read_receipts")
public class ReadReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private PrivateMessage message;

    @ManyToOne
    @JoinColumn(name = "reader_id", nullable = false)
    private User reader;

    @Column(nullable = false)
    private LocalDateTime readAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PrivateMessage getMessage() { return message; }
    public void setMessage(PrivateMessage message) { this.message = message; }

    public User getReader() { return reader; }
    public void setReader(User reader) { this.reader = reader; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}