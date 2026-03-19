package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  
  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  @Autowired
  private AuthService authService;

  @Autowired
  private UserService userService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
    logger.info("Registration attempt for username: {}", userDTO.getUsername());
    try {
      User user = userService.createUser(userDTO);
      logger.info("User registered successfully: {}", user.getUsername());
      return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    } catch (Exception e) {
      logger.error("Registration failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest,
                                   HttpServletResponse response) {
    logger.info("Login attempt for username: {}", loginRequest.getUsername());
    try {
      String token = authService.authenticate(loginRequest);

      // Устанавливаем JWT в Cookie
      Cookie cookie = new Cookie("jwtToken", token);
      cookie.setHttpOnly(true); // Защита от XSS атак
      cookie.setSecure(false); // true для HTTPS в продакшене
      cookie.setPath("/");
      cookie.setMaxAge(86400); // 24 часа в секундах
      response.addCookie(cookie);

      logger.info("User logged in successfully: {}", loginRequest.getUsername());
      return ResponseEntity.ok().body("Login successful");
    } catch (Exception e) {
      logger.error("Login failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
  }
}
