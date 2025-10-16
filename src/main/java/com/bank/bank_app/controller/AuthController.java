package com.bank.bank_app.controller;

import com.bank.bank_app.dto.AuthRequest;
import com.bank.bank_app.entity.User;
import com.bank.bank_app.repository.UserRepository;
import com.bank.bank_app.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "API для регистрации и входа пользователей")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/test-swagger")
    public String testSwagger() {
        return "Swagger should be available at: http://localhost:8081/swagger-ui.html";
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создает нового пользователя с ролью USER")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("ROLE_USER");

        userRepository.save(newUser);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/create-admin")
    @Operation(summary = "Создание администратора", description = "Создает пользователя с ролью ADMIN (для тестирования)")
    public ResponseEntity<?> createAdmin(@RequestBody AuthRequest request) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();

            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.badRequest().body("Admin already exists");
            }

            User admin = new User();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole("ROLE_ADMIN");

            userRepository.save(admin);
            return ResponseEntity.ok("Admin created successfully: username=" + username);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating admin: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение JWT токена")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();

            User foundUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(password, foundUser.getPassword())) {
                return ResponseEntity.badRequest().body("Invalid password");
            }

            String token = jwtService.generateToken(foundUser);
            return ResponseEntity.ok().body("{\"token\":\"" + token + "\"}");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
}