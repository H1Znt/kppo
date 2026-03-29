package com.example.demo.service;

import com.example.demo.dto.SensorDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Sensor;
import com.example.demo.model.User;
import com.example.demo.repository.SensorRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SensorService {
  private static final Logger logger = LoggerFactory.getLogger(SensorService.class);

  @Autowired
  private SensorRepository sensorRepository;

  @Autowired
  private UserRepository userRepository;

  @Transactional
  public Sensor createSensor(SensorDTO sensorDTO) {
    logger.info("Creating sensor at location: {}", sensorDTO.getLocation());

    Sensor sensor = new Sensor();
    sensor.setModel(sensorDTO.getModel());
    sensor.setLocation(sensorDTO.getLocation());

    if (sensorDTO.getAssignedToId() != null) {
      User user = userRepository.findById(sensorDTO.getAssignedToId())
        .orElseThrow(() -> {
          logger.error("User not found with ID: {}", sensorDTO.getAssignedToId());
          return new ResourceNotFoundException("User not found");
        });
      sensor.setAssignedTo(user);
    }

    Sensor saved = sensorRepository.save(sensor);
    logger.info("Sensor created with ID: {}", saved.getId());
    return saved; // возвращаем сохраненный датчик
  }

  public List<Sensor> getAllSensors() {
    logger.debug("Fetching all sensors");
    return sensorRepository.findAll();
  }

  public Sensor getSensorById(Long id) {
    logger.debug("Fetching sensor with ID: {}", id);
    return sensorRepository.findById(id)
      .orElseThrow(() -> {
        logger.error("Sensor not found with ID: {}", id);
        return new ResourceNotFoundException("Sensor not found");
      });
  }

  @Transactional
  public Sensor updateSensor(Long id, SensorDTO sensorDTO) {
    logger.info("Updating sensor ID: {}", id);
    Sensor sensor = sensorRepository.findById(id)
      .orElseThrow(() -> {
        logger.error("Sensor not found with ID: {}", id);
        return new ResourceNotFoundException("Sensor not found");
      });

    if (sensorDTO.getModel() != null) {
      sensor.setModel(sensorDTO.getModel());
    }

    if (sensorDTO.getLocation() != null) {
      sensor.setLocation(sensorDTO.getLocation());
    }

    if (sensorDTO.getAssignedToId() != null) {
      User user = userRepository.findById(sensorDTO.getAssignedToId())
      .orElseThrow(() -> {
        logger.error("User not found with ID: {}", sensorDTO.getAssignedToId());
        return new ResourceNotFoundException("User not found");
      });
      sensor.setAssignedTo(user);
    } else if (sensorDTO.getAssignedToId() == null && sensor.getAssignedTo() != null) {
      // Если передан null, снимаем назначение
      sensor.setAssignedTo(null);
    }

    Sensor saved = sensorRepository.save(sensor);
    logger.info("Sensor updated successfully with ID: {}", id);
    return saved;
  }

  @Transactional
  public void deleteSensor(Long id) {
    logger.info("Deleting sensor ID: {}", id);
    
    Sensor sensor = sensorRepository.findById(id)
      .orElseThrow(() -> {
        logger.error("Sensor not found with ID: {}", id);
        return new ResourceNotFoundException("Sensor not found");
      });
    
    // Проверяем, нет ли связанных Alert
    if (!sensor.getAlerts().isEmpty()) {
      logger.warn("Sensor has {} associated alerts. They will be deleted as well.", sensor.getAlerts().size());
    }

    sensorRepository.delete(sensor);
    logger.info("Sensor deleted successfully with ID: {}", id);
  }
}
