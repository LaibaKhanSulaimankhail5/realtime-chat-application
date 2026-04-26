package com.chat.chatapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username required")
    private String username;

    @NotBlank(message = "Email required")
    @Email(message = "Valid email required")
    private String email;

    @NotBlank(message = "Password required")
    @Size(min = 6, message = "Password must be 6 characters minimum")
    private String password;
}