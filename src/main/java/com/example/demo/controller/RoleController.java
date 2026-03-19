package com.example.demo.controller;

import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

  private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

  @Autowired
  private RoleRepository roleRepository;

  @GetMapping
  public ResponseEntity<List<Role>> getAllRoles() {
    logger.info("Fetching all roles");
    return ResponseEntity.ok(roleRepository.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
    logger.info("Fetching role with ID: {}", id);
    return ResponseEntity.ok(roleRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Role not found")));
  }
}
