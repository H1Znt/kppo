package com.example.demo.exception;

import java.util.Objects;

public class BadRequestException extends RuntimeException {

  private final String code;

  public BadRequestException(String code, String message) {
    super(message);
    this.code = Objects.requireNonNull(code, "code");
  }

  public String getCode() {
    return code;
  }
}
