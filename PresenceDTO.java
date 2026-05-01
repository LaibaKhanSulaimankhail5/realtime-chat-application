package com.chatapp.dto;

public class PresenceDTO {
    private String username;
    private boolean online;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
}