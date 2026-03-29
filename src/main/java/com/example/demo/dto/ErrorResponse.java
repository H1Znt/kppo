package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private int status;
  private String message;
  private LocalDateTime timestamp;
  private String path;

  public ErrorResponse(int status, String message, String path) {
    this.status = status;
    this.message = message;
    this.path = path;
    this.timestamp = LocalDateTime.now();
  }
}
