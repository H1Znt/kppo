package com.example.demo.controller;

import com.example.demo.dto.SensorDTO;
import com.example.demo.dto.SensorResponseDTO;
import com.example.demo.model.Sensor;
import com.example.demo.service.SensorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

  private static final Logger logger = LoggerFactory.getLogger(SensorController.class);

  @Autowired
  private SensorService sensorService;

  @PostMapping
  public ResponseEntity<SensorResponseDTO> createSensor(@Valid @RequestBody SensorDTO sensorDTO) {
    logger.info("Creating new sensor");
    Sensor sensor = sensorService.createSensor(sensorDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(toSensorResponseDTO(sensor));
  }

  @GetMapping
  public ResponseEntity<List<SensorResponseDTO>> getAllSensors() {
    logger.info("Fetching all sensors");
    List<SensorResponseDTO> sensors = sensorService.getAllSensors().stream()
      .map(this::toSensorResponseDTO)
      .collect(Collectors.toList());
    return ResponseEntity.ok(sensors);
  }

  @GetMapping("/{id}")
  public ResponseEntity<SensorResponseDTO> getSensorById(@PathVariable Long id) {
    logger.info("Fetching sensor with ID: {}", id);
    return ResponseEntity.ok(toSensorResponseDTO(sensorService.getSensorById(id)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<SensorResponseDTO> updateSensor(@PathVariable Long id, @Valid @RequestBody SensorDTO sensorDTO) {
    logger.info("Updating sensor ID: {}", id);
    Sensor sensor = sensorService.updateSensor(id, sensorDTO);
    return ResponseEntity.ok(toSensorResponseDTO(sensor));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteSensor(@PathVariable Long id) {
    logger.info("Deleting sensor ID: {}", id);
    sensorService.deleteSensor(id);
    return ResponseEntity.ok().body("Sensor deleted successfully");
  }

  private SensorResponseDTO toSensorResponseDTO(Sensor sensor) {
    Long assignedToId = sensor.getAssignedTo() != null ? sensor.getAssignedTo().getId() : null;
    String assignedToUsername = sensor.getAssignedTo() != null ? sensor.getAssignedTo().getUsername() : null;
    return new SensorResponseDTO(
      sensor.getId(),
      sensor.getModel(),
      sensor.getLocation(),
      assignedToId,
      assignedToUsername
    );
  }
}
