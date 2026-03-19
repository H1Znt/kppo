package com.example.demo.controller;

import com.example.demo.dto.AlertDTO;
import com.example.demo.dto.AlertResponseDTO;
import com.example.demo.model.StatusType;
import com.example.demo.service.AlertService;
import com.example.demo.service.PhotoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class AlertController {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private PhotoService photoService;
    @PostMapping
    public ResponseEntity<AlertResponseDTO> createIncident(@Valid @RequestBody AlertDTO alertDTO) {
      logger.info("Creating new incident");
      AlertResponseDTO response = alertService.createAlert(alertDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AlertResponseDTO>> getIncidents(@RequestParam(required = false) StatusType status) {
      logger.info("Fetching incidents with status: {}", status);
      List<AlertResponseDTO> incidents = alertService.getAlertsByStatus(status);
      return ResponseEntity.ok(incidents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponseDTO> getIncidentById(@PathVariable Long id) {
      logger.info("Fetching incident with ID: {}", id);
      AlertResponseDTO incident = alertService.getAlertById(id);
      return ResponseEntity.ok(incident);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<AlertResponseDTO> assignIncident(@PathVariable Long id, @RequestParam Long userId) {
      logger.info("Assigning incident ID: {} to user ID: {}", id, userId);
      AlertResponseDTO response = alertService.assignAlert(id, userId);
      return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status_change")
    public ResponseEntity<AlertResponseDTO> changeStatus(@PathVariable Long id, @RequestParam StatusType status) {
      logger.info("Changing status of incident ID: {} to {}", id, status);
      AlertResponseDTO response = alertService.changeStatus(id, status);
      return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<List<String>> uploadPhotos(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
      logger.info("Uploading photos for incident ID: {}", id);
      List<String> photoUrls = photoService.uploadPhotos(id, files);
      return ResponseEntity.ok(photoUrls);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertResponseDTO> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody AlertDTO alertDTO) {
      logger.info("Updating incident ID: {}", id);
      AlertResponseDTO response = alertService.updateAlert(id, alertDTO);
      return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncident(@PathVariable Long id) {
      logger.info("Deleting incident ID: {}", id);
      alertService.deleteAlert(id);
      return ResponseEntity.ok().body("Incident deleted successfully");
    }
}
