package com.chatapp.service;

import com.chatapp.model.FileAttachment;
import com.chatapp.model.User;
import com.chatapp.repository.FileAttachmentRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final UserRepository userRepository;

    private final String uploadDir = "uploads/";

    public FileAttachmentService(FileAttachmentRepository fileAttachmentRepository,
                                 UserRepository userRepository) {
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.userRepository = userRepository;
    }

    public FileAttachment uploadFile(MultipartFile file, String username) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Upload folder banao agar nahi hai
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        // Unique file naam banao
        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + uniqueFileName);
        Files.write(filePath, file.getBytes());

        FileAttachment attachment = new FileAttachment();
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFileUrl("/files/" + uniqueFileName);
        attachment.setUploadedBy(user);

        return fileAttachmentRepository.save(attachment);
    }

    public List<FileAttachment> getUserFiles(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return fileAttachmentRepository.findByUploadedBy(user);
    }
}