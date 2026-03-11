package com.example.demo.dto;

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
    
  @NotBlank(message = "Model is required")
  private String model;
  
  @NotBlank(message = "Location is required")
  private String location;
  
  private Long assignedToId; // ID пользователя, которому назначен датчик
}
