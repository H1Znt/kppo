package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorResponseDTO {
  private Long id;
  private String model;
  private String location;
  private Long assignedToId;
  private String assignedToUsername;
}
