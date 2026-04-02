package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.error.ApiErrorCodes;
import com.example.demo.exception.BadRequestException;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserService userService;

  @PostMapping
  public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
    logger.info("Creating user via API: {}", userDTO.getUsername());
    User user = userService.createUser(userDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponseDTO(user));
  }

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

  @PostMapping("/{id}/deactivate")
  public ResponseEntity<UserResponseDTO> deactivateUser(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails principal) {
    if (principal != null) {
      User current = userService.findByUsername(principal.getUsername());
      if (current.getId().equals(id)) {
        throw new BadRequestException(
            ApiErrorCodes.CANNOT_DEACTIVATE_SELF, "Cannot deactivate your own account");
      }
    }
    logger.info("Deactivating user ID: {}", id);
    return ResponseEntity.ok(toUserResponseDTO(userService.deactivateUser(id)));
  }

  @PostMapping("/{id}/activate")
  public ResponseEntity<UserResponseDTO> activateUser(@PathVariable Long id) {
    logger.info("Activating user ID: {}", id);
    return ResponseEntity.ok(toUserResponseDTO(userService.activateUser(id)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> permanentlyDeleteUser(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    User current = userService.findByUsername(principal.getUsername());
    if (current.getId().equals(id)) {
      throw new BadRequestException(
          ApiErrorCodes.CANNOT_DELETE_SELF, "Cannot delete your own account");
    }
    logger.info("Permanently deleting user ID: {}", id);
    userService.permanentlyDeleteUser(id);
    return ResponseEntity.ok().body("User permanently deleted");
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
