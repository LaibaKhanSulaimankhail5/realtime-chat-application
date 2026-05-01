package com.chatapp.service;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresenceService {

    private final UserRepository userRepository;

    public PresenceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setOnline(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOnline(true);
            userRepository.save(user);
        });
    }

    public void setOffline(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOnline(false);
            userRepository.save(user);
        });
    }

    public List<String> getOnlineUsers() {
        return userRepository.findAll()
                .stream()
                .filter(User::isOnline)
                .map(User::getUsername)
                .collect(Collectors.toList());
    }
}