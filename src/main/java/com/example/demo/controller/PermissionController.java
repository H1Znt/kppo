package com.example.demo.controller;

import com.example.demo.dto.PermissionResponseDTO;
import com.example.demo.error.ApiErrorCodes;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Permission;
import com.example.demo.repository.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

  private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

  @Autowired
  private PermissionRepository permissionRepository;

  @GetMapping
  public ResponseEntity<List<PermissionResponseDTO>> getAllPermissions() {
    logger.info("Fetching all permissions");
    List<PermissionResponseDTO> permissions = permissionRepository.findAll().stream()
      .map(this::toPermissionResponseDTO)
      .collect(Collectors.toList());
    return ResponseEntity.ok(permissions);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PermissionResponseDTO> getPermissionById(@PathVariable Long id) {
    logger.info("Fetching permission with ID: {}", id);
    Permission permission = permissionRepository.findById(id)
      .orElseThrow(
          () ->
              new ResourceNotFoundException(
                  ApiErrorCodes.PERMISSION_NOT_FOUND, "Permission not found"));
    return ResponseEntity.ok(toPermissionResponseDTO(permission));
  }

  private PermissionResponseDTO toPermissionResponseDTO(Permission permission) {
    return new PermissionResponseDTO(
      permission.getId(),
      permission.getPermission(),
      permission.getOperation()
    );
  }
}
