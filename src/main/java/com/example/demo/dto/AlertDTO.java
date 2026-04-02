package com.example.demo.dto;

import com.example.demo.error.ApiErrorCodes;
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
    
  @NotNull(message = ApiErrorCodes.ALERT_SENSOR_ID_REQUIRED)
  private Long sensorId;

  @ValidEventType(message = ApiErrorCodes.ALERT_EVENT_TYPE_INVALID)
  private String type;
  
  private String description;
  
  private LocalDateTime timestamp; // Если не указано, используется текущее время
}
