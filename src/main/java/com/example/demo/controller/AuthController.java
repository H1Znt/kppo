package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.RegisterUserDTO;
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
  public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDTO registerDTO) {
    logger.info("Registration attempt for username: {}", registerDTO.getUsername());
    UserDTO userDTO = new UserDTO(
        registerDTO.getUsername(),
        registerDTO.getPassword(),
        registerDTO.getRoleTitles());
    User user = userService.createUser(userDTO);
    logger.info("User registered successfully: {}", user.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest,
                                 HttpServletResponse response) {
    logger.info("Login attempt for username: {}", loginRequest.getUsername());
    String token = authService.authenticate(loginRequest);

    Cookie cookie = new Cookie("jwtToken", token);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(86400);
    response.addCookie(cookie);

    logger.info("User logged in successfully: {}", loginRequest.getUsername());
    return ResponseEntity.ok().body("Login successful");
  }
}
