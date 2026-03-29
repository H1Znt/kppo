package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeResponseDTO {
  private Long id;
  private String username;
  private boolean enabled;
  private Set<String> roleTitles;
  private Set<String> permissions;
}
