package com.example.demo.exception;

import java.util.Objects;

public class UnauthorizedException extends RuntimeException {

  private final String code;

  public UnauthorizedException(String code, String message) {
    super(message);
    this.code = Objects.requireNonNull(code, "code");
  }

  public String getCode() {
    return code;
  }
}
