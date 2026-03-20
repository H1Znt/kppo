package com.example.demo.dto;

import com.example.demo.model.EventType;
import com.example.demo.model.StatusType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponseDTO {
  private Long id;
  private Long sensorId;
  private String sensorLocation;
  private EventType type;
  private LocalDateTime timestamp;
  private String description;
  private StatusType status;
  private List<String> photoUrls;
  private String assignedToUsername; // Имя ответственного
  private String reportUrl; // URL PDF отчета (если статус RESOLVED)
}
