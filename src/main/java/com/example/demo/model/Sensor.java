package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String model;

  @Column(nullable = false, length = 200)
  private String location;

  @ManyToMany
  @JoinColumn(name = "assigned_user_id")
  private User assignedTo;

  @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL)
  private List<Alert> alerts = new ArrayList<>();
}
