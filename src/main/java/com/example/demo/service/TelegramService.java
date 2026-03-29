package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class TelegramService {

  private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);

  @Value("${telegram.bot.token:}")
  private String botToken;

  @Value("${telegram.bot.chat-id:}")
  private String chatId;

  private final RestClient restClient = RestClient.create();

  public void sendNotification(String message) {
    if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
      logger.warn("Telegram bot not configured, skipping notification");
      return;
    }
    try {
      restClient.post()
        .uri("https://api.telegram.org/bot{token}/sendMessage", botToken)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("chat_id", chatId, "text", message))
        .retrieve()
        .toBodilessEntity();
      logger.info("Telegram notification sent successfully");
    } catch (Exception e) {
      logger.error("Failed to send Telegram notification", e);
    }
  }

  public void sendAlertNotification(Long alertId, String sensorLocation, String eventType) {
    String text = String.format(
      "🚨 НОВЫЙ ИНЦИДЕНТ!\n\nID: %d\nМестоположение: %s\nТип события: %s\nВремя: %s",
      alertId,
      sensorLocation != null ? sensorLocation : "—",
      eventType,
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
    );
    sendNotification(text);
  }
}
