package com.example.demo.dto;

import com.example.demo.error.ApiErrorCodes;
import com.example.demo.validation.UniqueUsername;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserDTO {

  @NotBlank(message = ApiErrorCodes.REGISTER_USERNAME_BLANK)
  @Size(min = 3, max = 50, message = ApiErrorCodes.REGISTER_USERNAME_SIZE)
  @UniqueUsername(message = ApiErrorCodes.USERNAME_TAKEN)
  private String username;

  @NotBlank(message = ApiErrorCodes.REGISTER_PASSWORD_BLANK)
  @Size(min = 6, message = ApiErrorCodes.REGISTER_PASSWORD_SIZE)
  private String password;

  private Set<String> roleTitles;
}
