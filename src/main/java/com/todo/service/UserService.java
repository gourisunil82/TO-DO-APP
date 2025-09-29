package com.todo.service;

import com.todo.model.User;
import com.todo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> login(String username, String password) {
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        
        Optional<User> user = userRepository.findByUsername(username);
        System.out.println("User found: " + user.isPresent());
        
        if (user.isPresent()) {
            User foundUser = user.get();
            System.out.println("Stored password hash: " + foundUser.getPassword());
            System.out.println("Input password: " + password);
            
            boolean passwordMatches = passwordEncoder.matches(password, foundUser.getPassword());
            System.out.println("Password matches: " + passwordMatches);
            
            if (passwordMatches) {
                System.out.println("LOGIN SUCCESSFUL");
                return user;
            } else {
                System.out.println("PASSWORD MISMATCH");
            }
        } else {
            System.out.println("USER NOT FOUND");
        }
        
        System.out.println("=== LOGIN FAILED ===");
        return Optional.empty();
    }
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}