package com.example.demo.controller;

import com.example.demo.dto.SensorDTO;
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

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

  private static final Logger logger = LoggerFactory.getLogger(SensorController.class);

  @Autowired
  private SensorService sensorService;

  @PostMapping
  public ResponseEntity<Sensor> createSensor(@Valid @RequestBody SensorDTO sensorDTO) {
    logger.info("Creating new sensor");
    Sensor sensor = sensorService.createSensor(sensorDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(sensor);
  }

  @GetMapping
  public ResponseEntity<List<Sensor>> getAllSensors() {
    logger.info("Fetching all sensors");
    return ResponseEntity.ok(sensorService.getAllSensors());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Sensor> getSensorById(@PathVariable Long id) {
    logger.info("Fetching sensor with ID: {}", id);
    return ResponseEntity.ok(sensorService.getSensorById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Sensor> updateSensor(@PathVariable Long id, @Valid @RequestBody SensorDTO sensorDTO) {
    logger.info("Updating sensor ID: {}", id);
    Sensor sensor = sensorService.updateSensor(id, sensorDTO);
    return ResponseEntity.ok(sensor);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteSensor(@PathVariable Long id) {
    logger.info("Deleting sensor ID: {}", id);
    sensorService.deleteSensor(id);
    return ResponseEntity.ok().body("Sensor deleted successfully");
  }
}
