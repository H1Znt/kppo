package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserService userService;

  @GetMapping
  public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
    logger.info("Fetching all users");
    List<UserResponseDTO> users = userService.getAllUsers().stream()
      .map(this::toUserResponseDTO)
      .collect(Collectors.toList());
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
    logger.info("Fetching user with ID: {}", id);
    return ResponseEntity.ok(toUserResponseDTO(userService.findById(id)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
    logger.info("Updating user ID: {}", id);
    User user = userService.updateUser(id, userDTO);
    return ResponseEntity.ok(toUserResponseDTO(user));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    logger.info("Deleting user ID: {}", id);
    userService.deleteUser(id);
    return ResponseEntity.ok().body("User deleted successfully");
  }

  private UserResponseDTO toUserResponseDTO(User user) {
    return new UserResponseDTO(
      user.getId(),
      user.getUsername(),
      user.isEnabled(),
      user.getRoles().stream().map(role -> role.getTitle()).collect(Collectors.toSet())
    );
  }
}
