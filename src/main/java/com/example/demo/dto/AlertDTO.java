package com.example.demo.dto;

import com.example.demo.validation.ValidEventType;
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
  
  @ValidEventType(message = "Invalid event type (use ACCIDENT, HARD_BRAKING, BUTTON)")
  private String type;
  
  private String description;
  
  private LocalDateTime timestamp; // Если не указано, используется текущее время
}
