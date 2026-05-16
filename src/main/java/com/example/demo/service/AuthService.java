package com.example.demo.service;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.error.ApiErrorCodes;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.Token;
import com.example.demo.model.User;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private TokenRepository tokenRepository;

  @Transactional
  public void authenticate(LoginRequestDTO loginRequest, HttpServletResponse response) {
    logger.info("Authentication attempt for username: {}", loginRequest.getUsername());

    User user = userRepository.findByUsername(loginRequest.getUsername())
      .orElseThrow(() -> {
        logger.error("Login failed, user not found: {}", loginRequest.getUsername());
        return new UnauthorizedException(ApiErrorCodes.LOGIN_USER_NOT_FOUND, "User not found");
      });

    if (!user.isEnabled()) {
      logger.error("User is disabled: {}", loginRequest.getUsername());
      throw new UnauthorizedException(ApiErrorCodes.LOGIN_ACCOUNT_DISABLED, "Account is disabled");
    }

    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      logger.error("Invalid password for username: {}", loginRequest.getUsername());
      throw new UnauthorizedException(ApiErrorCodes.LOGIN_INVALID_PASSWORD, "Invalid password");
    }

    // Access token — не хранится в БД, проверяется только по подписи
    String accessTokenValue = jwtUtil.generateAccessToken(user.getUsername());

    // Refresh token — хранится в БД для контроля отзыва
    String refreshTokenValue = UUID.randomUUID().toString();
    tokenRepository.save(new Token(
            null,
            refreshTokenValue,
            LocalDateTime.now().plusDays(7),
            false,
            user
    ));

    addCookie(response, "access_token",  accessTokenValue,  3600);   // 1 час
    addCookie(response, "refresh_token", refreshTokenValue, 604800); // 7 дней

    logger.info("User authenticated successfully: {}", loginRequest.getUsername());
  }

  @Transactional
  public void refresh(String refreshTokenValue, HttpServletResponse response) {
    // UUID ищем в БД
    Token refreshToken = tokenRepository.findByTokenValue(refreshTokenValue)
      .orElseThrow(() -> new UnauthorizedException(
          ApiErrorCodes.LOGIN_INVALID_PASSWORD, "Refresh token not found"));

    if (refreshToken.isRevoked()) {
      throw new UnauthorizedException(ApiErrorCodes.LOGIN_INVALID_PASSWORD, "Refresh token has been revoked");
    }

    if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
      throw new UnauthorizedException(ApiErrorCodes.LOGIN_INVALID_PASSWORD, "Refresh token has expired");
    }

    // Отзываем использованный
    refreshToken.setRevoked(true);
    tokenRepository.save(refreshToken);

    User user = refreshToken.getUser();

    // Новый access token (JWT)
    String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());

    // Новый refresh token (UUID) — сохраняем в БД
    String newRefreshToken = UUID.randomUUID().toString();
    tokenRepository.save(new Token(
            null,
            newRefreshToken,
            LocalDateTime.now().plusDays(7),
            false,
            user
    ));

    addCookie(response, "access_token",  newAccessToken,  3600);
    addCookie(response, "refresh_token", newRefreshToken, 604800);

    logger.info("Tokens refreshed for user: {}", user.getUsername());
  }

  @Transactional
  public void logout(String refreshTokenValue, HttpServletResponse response) {
    // Access в БД нет, просто удаляем
    // Refresh в БД, отзываем его
    if (refreshTokenValue != null) {
      tokenRepository.findByTokenValue(refreshTokenValue)
        .ifPresent(t -> { t.setRevoked(true); tokenRepository.save(t); });
    }
    clearCookie(response, "access_token");
    clearCookie(response, "refresh_token");
    logger.info("User logged out, refresh token revoked");
  }

  private void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // true в production
    cookie.setPath("/");
    cookie.setMaxAge(maxAgeSeconds);
    response.addCookie(cookie);
  }

  private void clearCookie(HttpServletResponse response, String name) {
    Cookie cookie = new Cookie(name, "");
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
}
