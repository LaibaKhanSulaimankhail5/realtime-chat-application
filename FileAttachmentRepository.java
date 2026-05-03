package com.chatapp.repository;

import com.chatapp.model.FileAttachment;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    List<FileAttachment> findByUploadedBy(User user);
}