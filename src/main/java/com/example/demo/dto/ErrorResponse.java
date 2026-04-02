package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
  private int status;
  private String code;
  private String message;
  private LocalDateTime timestamp;
  private String path;
  // Коды полей (Bean Validation)
  private Map<String, String> errors;

  public ErrorResponse(int status, String code, String message, String path) {
    this(status, code, message, path, null);
  }

  public ErrorResponse(int status, String code, String message, String path, Map<String, String> errors) {
    this.status = status;
    this.code = code;
    this.message = message;
    this.path = path;
    this.timestamp = LocalDateTime.now();
    this.errors = errors;
  }
}
