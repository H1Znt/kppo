package com.example.demo.dto;

import com.example.demo.error.ApiErrorCodes;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorDTO {
    
  @NotBlank(message = ApiErrorCodes.SENSOR_MODEL_REQUIRED)
  private String model;

  @NotBlank(message = ApiErrorCodes.SENSOR_LOCATION_REQUIRED)
  private String location;
  
  private Long assignedToId; // ID пользователя, которому назначен датчик
}
