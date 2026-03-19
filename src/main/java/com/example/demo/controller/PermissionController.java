package com.example.demo.controller;

import com.example.demo.model.Permission;
import com.example.demo.repository.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

  private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

  @Autowired
  private PermissionRepository permissionRepository;

  @GetMapping
  public ResponseEntity<List<Permission>> getAllPermissions() {
    logger.info("Fetching all permissions");
    return ResponseEntity.ok(permissionRepository.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Permission> getPermissionById(@PathVariable Long id) {
    logger.info("Fetching permission with ID: {}", id);
    return ResponseEntity.ok(permissionRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Permission not found")));
  }
}
