package com.bank.bank_app.controller;

import com.bank.bank_app.dto.AuthRequest;
import com.bank.bank_app.entity.User;
import com.bank.bank_app.repository.UserRepository;
import com.bank.bank_app.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_Success() {
        AuthRequest request = new AuthRequest("testuser", "password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyExists() {
        AuthRequest request = new AuthRequest("existinguser", "password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already exists", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        AuthRequest request = new AuthRequest("testuser", "password123");

        User foundUser = new User();
        foundUser.setUsername("testuser");
        foundUser.setPassword("encodedPassword");
        foundUser.setRole("ROLE_USER");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(foundUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(foundUser)).thenReturn("test-jwt-token");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("test-jwt-token"));
    }

    @Test
    void login_InvalidPassword() {
        AuthRequest request = new AuthRequest("testuser", "wrongpassword");

        User foundUser = new User();
        foundUser.setUsername("testuser");
        foundUser.setPassword("encodedPassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(foundUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid password", response.getBody());
    }

    @Test
    void createAdmin_Success() {
        AuthRequest request = new AuthRequest("admin", "admin123");

        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        ResponseEntity<?> response = authController.createAdmin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Admin created successfully"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createAdmin_AlreadyExists() {
        AuthRequest request = new AuthRequest("admin", "admin123");

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        ResponseEntity<?> response = authController.createAdmin(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Admin already exists", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }
}