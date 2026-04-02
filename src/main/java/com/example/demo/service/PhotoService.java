package com.example.demo.service;

import com.example.demo.error.ApiErrorCodes;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Alert;
import com.example.demo.repository.AlertRepository;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PhotoService {

  private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);

  @Value("${app.upload.dir:uploads}")
  private String uploadDir;

  @Autowired
  private AlertRepository alertRepository;

  public List<String> uploadPhotos(Long alertId, List<MultipartFile> files) {
    logger.info("Uploading {} photos for alert ID: {}", files.size(), alertId);

    Alert alert = alertRepository.findById(alertId)
      .orElseThrow(() -> {
        logger.error("Alert not found with ID: {}", alertId);
        return new ResourceNotFoundException(ApiErrorCodes.ALERT_NOT_FOUND, "Alert not found");
      });

    List<String> photoUrls = new ArrayList<>();

    try {
      Path uploadPath = Paths.get(uploadDir, "alerts", alertId.toString());
      Files.createDirectories(uploadPath);

      for (MultipartFile file : files) {
        if (file.isEmpty()) continue;

        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;

        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String photoUrl = "/uploads/alerts/" + alertId + "/" + newFilename;
        photoUrls.add(photoUrl);
        logger.info("Photo saved: {}", photoUrl);
      }

      alert.getPhotoUrls().addAll(photoUrls);
      alertRepository.save(alert);
      
    } catch (IOException e) {
      logger.error("Error uploading photos", e);
      throw new RuntimeException("Failed to upload photos", e);
    }

    return photoUrls;
  }

  @Transactional
  public void deletePhoto(Long alertId, String photoUrl) {
    if (photoUrl == null || photoUrl.isBlank()) {
      throw new BadRequestException(ApiErrorCodes.PHOTO_URL_REQUIRED, "Photo URL is required");
    }

    Alert alert = alertRepository.findById(alertId)
        .orElseThrow(() -> {
          logger.error("Alert not found with ID: {}", alertId);
          return new ResourceNotFoundException(ApiErrorCodes.ALERT_NOT_FOUND, "Alert not found");
        });

    if (!alert.getPhotoUrls().contains(photoUrl)) {
      throw new BadRequestException(
          ApiErrorCodes.PHOTO_URL_NOT_FOR_INCIDENT, "Photo URL does not belong to this incident");
    }

    Path filePath = resolvePhotoPath(alertId, photoUrl);
    try {
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      logger.error("Failed to delete photo file: {}", filePath, e);
      throw new RuntimeException("Failed to delete photo file", e);
    }

    alert.getPhotoUrls().remove(photoUrl);
    alertRepository.save(alert);
    logger.info("Photo removed from alert ID {}: {}", alertId, photoUrl);
  }

  // Удаление каталога на диске после удаления инцидента
  public void deleteAlertDirectoryOnDisk(Long alertId) {
    Path dir = Paths.get(uploadDir, "alerts", alertId.toString()).normalize();
    Path base = Paths.get(uploadDir, "alerts").normalize();
    if (!dir.startsWith(base) || !Files.isDirectory(dir)) {
      return;
    }
    try {
      if (Files.exists(dir)) {
        try (Stream<Path> walk = Files.walk(dir)) {
          walk.sorted(Comparator.reverseOrder()).forEach(p -> {
            try {
              Files.deleteIfExists(p);
            } catch (IOException e) {
              logger.warn("Could not delete {}", p, e);
            }
          });
        }
      }
    } catch (IOException e) {
      logger.error("Failed to delete alert photo directory: {}", dir, e);
    }
  }

  private Path resolvePhotoPath(long alertId, String photoUrl) {
    String prefix = "/uploads/alerts/" + alertId + "/";
    if (!photoUrl.startsWith(prefix)) {
      throw new BadRequestException(
          ApiErrorCodes.PHOTO_URL_INVALID, "Invalid photo URL for this incident");
    }
    String filename = photoUrl.substring(prefix.length());
    if (filename.isEmpty() || filename.contains("..") || filename.indexOf('/') >= 0 || filename.indexOf('\\') >= 0) {
      throw new BadRequestException(ApiErrorCodes.PHOTO_PATH_INVALID, "Invalid photo path");
    }
    Path dir = Paths.get(uploadDir, "alerts", Long.toString(alertId)).normalize();
    Path file = dir.resolve(filename).normalize();
    if (!file.startsWith(dir)) {
      throw new BadRequestException(ApiErrorCodes.PHOTO_PATH_INVALID, "Invalid photo path");
    }
    return file;
  }
}
