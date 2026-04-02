package com.example.demo.validation;

import com.example.demo.error.ApiErrorCodes;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventTypeValidator.class)
@Documented
public @interface ValidEventType {
  String message() default ApiErrorCodes.ALERT_EVENT_TYPE_INVALID;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
