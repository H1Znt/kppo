package com.example.demo.controller;

import com.example.demo.dto.RoleResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

  private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

  @Autowired
  private RoleRepository roleRepository;

  @GetMapping
  public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {
    logger.info("Fetching all roles");
    List<RoleResponseDTO> roles = roleRepository.findAll().stream()
      .map(this::toRoleResponseDTO)
      .collect(Collectors.toList());
    return ResponseEntity.ok(roles);
  }

  @GetMapping("/{id}")
  public ResponseEntity<RoleResponseDTO> getRoleById(@PathVariable Long id) {
    logger.info("Fetching role with ID: {}", id);
    Role role = roleRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    return ResponseEntity.ok(toRoleResponseDTO(role));
  }

  private RoleResponseDTO toRoleResponseDTO(Role role) {
    return new RoleResponseDTO(
      role.getId(),
      role.getTitle(),
      role.getPermissions().stream().map(permission -> permission.getPermission()).collect(Collectors.toSet())
    );
  }
}
