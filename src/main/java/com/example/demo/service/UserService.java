package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Transactional
  public User createUser(UserDTO userDTO) {
    logger.info("Creating user with username: {}", userDTO.getUsername());

    if (userRepository.existsByUsername(userDTO.getUsername())) {
      logger.error("Username already exists: {}", userDTO.getUsername());
      throw new RuntimeException("Username already exists");
    }

    User user = new User();
    user.setUsername(userDTO.getUsername());
    user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
    user.setEnabled(true);

    if (userDTO.getRoleTitles() != null && !userDTO.getRoleTitles().isEmpty()) {
      Set<Role> roles = userDTO.getRoleTitles().stream()
        .map(roleTitle -> roleRepository.findByTitle(roleTitle)
          .orElseThrow(() -> {
            logger.error("Role not found: {}", roleTitle);
            return new RuntimeException("Role not found: " + roleTitle);
          }))
        .collect(Collectors.toSet());
      user.setRoles(roles);
    }

    User saved = userRepository.save(user);
    logger.info("User created successfully with ID: {}", saved.getId());
    return saved;
  }

  public User findByUsername(String username) {
    logger.debug("Finding user by username: {}", username);
    return userRepository.findByUsername(username)
      .orElseThrow(() -> {
        logger.error("User not found: {}", username);
        return new RuntimeException("User not found");
      });
  }

  public User findById(Long id) {
    logger.debug("Finding user by ID: {}", id);
    return userRepository.findById(id)
      .orElseThrow(() -> {
        logger.error("User not found with ID: {}", id);
        return new RuntimeException("User not found");
      });
  }

  public List<User> getAllUsers() {
    logger.debug("Fetching all users");
    return userRepository.findAll();
  }

  @Transactional
  public User updateUser(Long id, UserDTO userDTO) {
    logger.info("Updating user ID: {}", id);

    User user = userRepository.findById(id).orElseThrow(() -> {
      logger.error("User not found with ID: {}", id);
      return new RuntimeException("User not found");
    });

    // Обновляем username только если он изменился и не занят
    if (userDTO.getUsername() != null && !userDTO.getUsername().equals(user.getUsername())) {
      if (userRepository.existsByUsername(userDTO.getUsername())) {
        logger.error("Username already exists: {}", userDTO.getUsername());
        throw new RuntimeException("Username already exists");
      }
      user.setUsername(userDTO.getUsername());
    }
    
    // Обновляем пароль только если он указан
    if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
      user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
    }
    
    // Обновляем роли
    if (userDTO.getRoleTitles() != null) {
      Set<Role> roles = userDTO.getRoleTitles().stream()
        .map(roleTitle -> roleRepository.findByTitle(roleTitle)
          .orElseThrow(() -> {
            logger.error("Role not found: {}", roleTitle);
            return new RuntimeException("Role not found: " + roleTitle);
          }))
        .collect(Collectors.toSet());
      user.setRoles(roles);
    }
    
    User saved = userRepository.save(user);
    logger.info("User updated successfully with ID: {}", id);
    return saved;
  }

  @Transactional
  public void deleteUser(Long id) {
    logger.info("Deleting user ID: {}", id);

    User user = userRepository.findById(id).orElseThrow(() -> {
      logger.error("User not found with ID: {}", id);
      return new RuntimeException("User not found");
    });

    // Деактивируем пользователя
    user.setEnabled(false);
    userRepository.save(user);

    logger.info("User deactivated successfully with ID: {}", id);
  }
}
