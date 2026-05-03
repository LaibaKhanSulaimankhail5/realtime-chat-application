package com.chatapp.controller;

import com.chatapp.model.FileAttachment;
import com.chatapp.service.FileAttachmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileAttachmentController {

    private final FileAttachmentService fileAttachmentService;

    public FileAttachmentController(FileAttachmentService fileAttachmentService) {
        this.fileAttachmentService = fileAttachmentService;
    }

    @PostMapping("/upload/{username}")
    public ResponseEntity<FileAttachment> uploadFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable String username) {
        try {
            FileAttachment attachment = fileAttachmentService.uploadFile(file, username);
            return ResponseEntity.ok(attachment);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{username}")
    public List<FileAttachment> getUserFiles(@PathVariable String username) {
        return fileAttachmentService.getUserFiles(username);
    }
}