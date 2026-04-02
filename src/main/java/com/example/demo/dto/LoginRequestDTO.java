package com.example.demo.dto;

import com.example.demo.error.ApiErrorCodes;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    
  @NotBlank(message = ApiErrorCodes.LOGIN_USERNAME_BLANK)
  private String username;

  @NotBlank(message = ApiErrorCodes.LOGIN_PASSWORD_BLANK)
  private String password;
}
