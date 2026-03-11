package com.example.demo.dto;

import com.example.demo.model.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    
  @NotNull(message = "Sensor ID is required")
  private Long sensorId;
  
  @NotNull(message = "Event type is required")
  private EventType type;
  
  private String description;
  
  private LocalDateTime timestamp; // Если не указано, используется текущее время
}
