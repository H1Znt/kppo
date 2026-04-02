package com.example.demo.validation;

import com.example.demo.error.ApiErrorCodes;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Documented
public @interface UniqueUsername {
  String message() default ApiErrorCodes.USERNAME_TAKEN;
  
  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
