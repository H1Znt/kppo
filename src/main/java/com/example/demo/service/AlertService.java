package com.example.demo.service;

import com.example.demo.dto.AlertDTO;
import com.example.demo.dto.AlertResponseDTO;
import com.example.demo.error.ApiErrorCodes;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Alert;
import com.example.demo.model.EventType;
import com.example.demo.model.Sensor;
import com.example.demo.model.StatusType;
import org.slf4j.Logger;
import com.example.demo.repository.AlertRepository;
import com.example.demo.repository.SensorRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {
  private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

  @Autowired
  private AlertRepository alertRepository;

  @Autowired
  private SensorRepository sensorRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private PDFService pdfService;

  @Autowired
  private PhotoService photoService;

  @Autowired
  private TelegramService telegramService;

  @Transactional
  public AlertResponseDTO createAlert(AlertDTO alertDTO) {
    logger.info("Creating new alert for sensor ID: {}", alertDTO.getSensorId());

    if (alertDTO.getType() == null || alertDTO.getType().isBlank()) {
      throw new BadRequestException(ApiErrorCodes.EVENT_TYPE_REQUIRED, "Event type is required");
    }

    Sensor sensor = sensorRepository.findById(alertDTO.getSensorId())
      .orElseThrow(() -> {
        logger.error("Sensor not found with ID: {}", alertDTO.getSensorId());
        return new ResourceNotFoundException(ApiErrorCodes.SENSOR_NOT_FOUND, "Sensor not found");
      });

    EventType eventType = parseEventType(alertDTO.getType());
    Alert alert = new Alert();
    alert.setSensor(sensor);
    alert.setType(eventType);
    alert.setTimestamp(alertDTO.getTimestamp() != null ? alertDTO.getTimestamp() : LocalDateTime.now());
    alert.setDescription(alertDTO.getDescription());
    alert.setStatus(StatusType.NEW);

    Alert savedAlert = alertRepository.save(alert);
    logger.info("Alert created with ID: {}", savedAlert.getId());

    telegramService.sendAlertNotification(
      savedAlert.getId(),
      sensor.getLocation(),
      eventType.name()
    );

    return convertToDTO(savedAlert);
  }
  
  public List<AlertResponseDTO> getAlertsByStatus(StatusType status) {
    logger.debug("Fetching alerts with status: {}", status);
    List<Alert> alerts;
    if (status != null) {
      alerts = alertRepository.findByStatusOrderByTimestampDesc(status);
    } else {
      alerts = alertRepository.findAll();
    }
    return alerts.stream()
      .map(this::convertToDTO)
      .collect(Collectors.toList());
  }

  @Transactional
  public AlertResponseDTO assignAlert(Long alertId, Long userId) {
    logger.info("Assigning alert ID: {} to user ID: {}", alertId, userId);

    Alert alert = alertRepository.findById(alertId)
      .orElseThrow(() -> {
        logger.error("Alert not found with ID: {}", alertId);
        return new ResourceNotFoundException(ApiErrorCodes.ALERT_NOT_FOUND, "Alert not found");
      });

    Sensor sensor = alert.getSensor();
    // Назначаем пользователя на датчик
    sensor.setAssignedTo(userService.findById(userId));
    sensorRepository.save(sensor);
    logger.info("Alert assigned successfully");
    return convertToDTO(alert);
  }

  @Transactional
  public AlertResponseDTO changeStatus(Long alertId, StatusType newStatus) {
    logger.info("Changing status of alert ID: {} to {}", alertId, newStatus);

    Alert alert = alertRepository.findById(alertId)
      .orElseThrow(() -> {
        logger.error("Alert not found with ID: {}", alertId);
        return new ResourceNotFoundException(ApiErrorCodes.ALERT_NOT_FOUND, "Alert not found");
      });

    StatusType previousStatus = alert.getStatus();
    alert.setStatus(newStatus);
    Alert saved = alertRepository.save(alert);

    // Автоматически генерируем PDF при закрытии инцидента
    if (newStatus == StatusType.RESOLVED) {
      try {
        String reportUrl = pdfService.generateAlertReport(saved);
        saved.setReportUrl(reportUrl);
        saved = alertRepository.save(saved);
        logger.info("PDF report generated for alert ID: {}, URL: {}", alertId, reportUrl);
      } catch (Exception e) {
        logger.error("Failed to generate PDF report for alert ID: {}", alertId, e);
        // Не прерываем выполнение, если PDF не удалось создать — reportUrl в БД не ставим
      }
    }

    if (previousStatus != newStatus) {
      try {
        Sensor sensor = saved.getSensor();
        telegramService.sendAlertStatusChangeNotification(
            saved.getId(),
            previousStatus,
            newStatus,
            saved.getType(),
            sensor != null ? sensor.getLocation() : null);
      } catch (Exception e) {
        logger.warn("Telegram status notification skipped or failed for alert ID: {}", alertId, e);
      }
    }

    logger.info("Status changed successfully for alert ID: {}", alertId);
    return convertToDTO(saved);
  }

  public AlertResponseDTO getAlertById(Long id) {
    logger.debug("Fetching alert with ID: {}", id);
    Alert alert = alertRepository.findById(id)
      .orElseThrow(() -> {
        logger.error("Alert not found with ID: {}", id);
        return new ResourceNotFoundException(ApiErrorCodes.ALERT_NOT_FOUND, "Alert not found");
      });
    return convertToDTO(alert);
  }

  @Transactional
  public AlertResponseDTO updateAlert(Long id, AlertDTO alertDTO) {
    logger.info("Updating alert ID: {}", id);
    Alert alert = alertRepository.findById(id)
      .orElseThrow(() -> {
        logger.error("Alert not found with ID: {}", id);
        return new ResourceNotFoundException(ApiErrorCodes.ALERT_NOT_FOUND, "Alert not found");
      });

    // Обновляем только изменяемые поля
    if (alertDTO.getSensorId() != null) {
      Sensor sensor = sensorRepository.findById(alertDTO.getSensorId())
        .orElseThrow(() -> {
          logger.error("Sensor not found with ID: {}", alertDTO.getSensorId());
          return new ResourceNotFoundException(ApiErrorCodes.SENSOR_NOT_FOUND, "Sensor not found");
        });
      alert.setSensor(sensor);
    }
    if (alertDTO.getType() != null) {
      alert.setType(parseEventType(alertDTO.getType()));
    }
    if (alertDTO.getDescription() != null) {
      alert.setDescription(alertDTO.getDescription());
    }
    if (alertDTO.getTimestamp() != null) {
      alert.setTimestamp(alertDTO.getTimestamp());
    }
    Alert saved = alertRepository.save(alert);
    logger.info("Alert updated successfully with ID: {}", id);
    return convertToDTO(saved);
  }

  @Transactional
  public void deleteAlert(Long id) {
    logger.info("Deleting alert ID: {}", id);

    Alert alert = alertRepository.findById(id)
      .orElseThrow(() -> {
        logger.error("Alert not found with ID: {}", id);
        return new ResourceNotFoundException(ApiErrorCodes.ALERT_NOT_FOUND, "Alert not found");
      });
    alertRepository.delete(alert);
    logger.info("Alert deleted successfully with ID: {}", id);

    photoService.deleteAlertDirectoryOnDisk(id);
    pdfService.deleteReportPdf(id);
  }

  // Удаление PDF-отчёта с диска и очистка поля reportUrl у инцидента
  @Transactional
  public AlertResponseDTO deleteIncidentReport(Long alertId) {
    Alert alert = alertRepository.findById(alertId)
        .orElseThrow(() -> {
          logger.error("Alert not found with ID: {}", alertId);
          return new ResourceNotFoundException(ApiErrorCodes.ALERT_NOT_FOUND, "Alert not found");
        });
    pdfService.deleteReportPdf(alertId);
    alert.setReportUrl(null);
    Alert saved = alertRepository.save(alert);
    logger.info("Report URL cleared for alert ID: {}", alertId);
    return convertToDTO(saved);
  }

  public AlertResponseDTO convertToDTO(Alert alert) {
    AlertResponseDTO dto = new AlertResponseDTO();
    dto.setId(alert.getId());
    dto.setSensorId(alert.getSensor().getId());
    dto.setSensorLocation(alert.getSensor().getLocation());
    dto.setType(alert.getType());
    dto.setTimestamp(alert.getTimestamp());
    dto.setDescription(alert.getDescription());
    dto.setStatus(alert.getStatus());
    dto.setPhotoUrls(alert.getPhotoUrls());
    if (alert.getSensor().getAssignedTo() != null) {
      dto.setAssignedToUsername(alert.getSensor().getAssignedTo().getUsername());
    }

    dto.setReportUrl(alert.getReportUrl());
    return dto;
  }

  private static EventType parseEventType(String raw) {
    return EventType.valueOf(raw.trim().toUpperCase());
  }
}
