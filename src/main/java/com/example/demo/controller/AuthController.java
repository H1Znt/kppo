package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.MeResponseDTO;
import com.example.demo.dto.RegisterUserDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
  public ResponseEntity<LoginResponseDTO> login(
          @Valid @RequestBody LoginRequestDTO loginRequest,
          HttpServletResponse response) {
    logger.info("Login attempt for username: {}", loginRequest.getUsername());
    authService.authenticate(loginRequest, response); // куки ставятся внутри сервиса
    logger.info("User logged in successfully: {}", loginRequest.getUsername());
    return ResponseEntity.ok(new LoginResponseDTO("Login successful", null));
  }

  // Выдаёт новый access_token по refresh_token из куки
  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(
          @CookieValue(name = "refresh_token", required = false) String refreshToken,
          HttpServletResponse response) {
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token not found");
    }
    authService.refresh(refreshToken, response);
    return ResponseEntity.ok("Token refreshed");
  }

  // Выход — отзывает refresh token в БД и удаляет access token
  @PostMapping("/logout")
  public ResponseEntity<?> logout(
          @CookieValue(name = "refresh_token", required = false) String refreshToken,
          HttpServletResponse response) {
    authService.logout(refreshToken, response);
    return ResponseEntity.ok("Logged out");
  }

  @GetMapping("/me")
  public ResponseEntity<MeResponseDTO> me(@AuthenticationPrincipal UserDetails principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(userService.getMe(principal.getUsername()));
  }
}
