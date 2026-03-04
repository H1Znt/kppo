package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToMany
  @JoinColumn(name = "sensor_id", nullable = false)
  private Sensor sensor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private EventType type; // Тип события

  @Column(nullable = false)
  private LocalDateTime timestamp; // Время события

  @Column(length = 1000)
  private String description; // Описание инцидента

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatusType status = StatusType.NEW; // Статус по умолчанию

  @ElementCollection
  @CollectionTable(name = "alert_photos", joinColumns = @JoinColumn(name = "alert_id"))
  @Column(name = "photo_url")
  private List<String> photoUrls = new ArrayList<>();
}
