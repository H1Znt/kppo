package com.example.demo.validation;

import com.example.demo.model.EventType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class EventTypeValidator implements ConstraintValidator<ValidEventType, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true; // пустоту отсекает @NotBlank на поле
    }
    try {
      EventType.valueOf(value.trim().toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
