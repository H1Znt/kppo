package com.example.demo.service;

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
import java.util.List;
import java.util.UUID;

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
        return new RuntimeException("Alert not found");
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
}
