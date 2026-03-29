package com.example.demo.service;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtil jwtUtil;

  public String authenticate(LoginRequestDTO loginRequest) {
    logger.info("Authentication attempt for username: {}", loginRequest.getUsername());

    User user = userRepository.findByUsername(loginRequest.getUsername())
      .orElseThrow(() -> {
        logger.error("Invalid credentials for username: {}", loginRequest.getUsername());
        return new UnauthorizedException("Invalid credentials");
      });

    if (!user.isEnabled()) {
      logger.error("User is disabled: {}", loginRequest.getUsername());
      throw new UnauthorizedException("User is disabled");
    }

    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      logger.error("Invalid password for username: {}", loginRequest.getUsername());
      throw new UnauthorizedException("Invalid credentials");
    }

    String token = jwtUtil.generateToken(user.getUsername());
    logger.info("User authenticated successfully: {}", loginRequest.getUsername());
    return token; // Для возврата токена в контроллер
  }
}
