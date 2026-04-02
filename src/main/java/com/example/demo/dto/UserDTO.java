package com.example.demo.dto;

import com.example.demo.error.ApiErrorCodes;
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
public class UserDTO {
    
  @NotBlank(message = ApiErrorCodes.USER_USERNAME_BLANK)
  @Size(min = 3, max = 50, message = ApiErrorCodes.USER_USERNAME_SIZE)
  private String username;

  @Size(min = 6, message = ApiErrorCodes.USER_PASSWORD_SIZE)
  private String password;
  
  private Set<String> roleTitles; // Названия ролей
}
