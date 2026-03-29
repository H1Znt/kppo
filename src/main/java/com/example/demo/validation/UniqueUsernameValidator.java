package com.example.demo.validation;

import com.example.demo.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

  @Autowired
  private UserRepository userRepository;

  @Override
  public boolean isValid(String username, ConstraintValidatorContext context) {
    if (username == null || username.isBlank()) {
      return true;
    }
    return !userRepository.existsByUsername(username.trim());
  }
}
