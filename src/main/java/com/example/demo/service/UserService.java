package com.example.demo.service;

import com.example.demo.dto.MeResponseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.error.ApiErrorCodes;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.Sensor;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.SensorRepository;
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
  private SensorRepository sensorRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Transactional
  public User createUser(UserDTO userDTO) {
    logger.info("Creating user with username: {}", userDTO.getUsername());

    if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
      throw new BadRequestException(ApiErrorCodes.PASSWORD_REQUIRED, "Password is required");
    }

    if (userRepository.existsByUsername(userDTO.getUsername())) {
      logger.error("Username already exists: {}", userDTO.getUsername());
      throw new BadRequestException(ApiErrorCodes.USERNAME_TAKEN, "Username already taken");
    }

    User user = new User();
    user.setUsername(userDTO.getUsername());
    user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
    user.setEnabled(true);

    if (userDTO.getRoleTitles() == null || userDTO.getRoleTitles().isEmpty()) {
      throw new BadRequestException(ApiErrorCodes.ROLE_REQUIRED, "Select one role");
    }
    if (userDTO.getRoleTitles().size() != 1) {
      throw new BadRequestException(ApiErrorCodes.ROLE_SINGLE_REQUIRED, "Exactly one role required");
    }
    Set<Role> roles = userDTO.getRoleTitles().stream()
      .map(roleTitle -> roleRepository.findByTitle(roleTitle)
        .orElseThrow(() -> {
          logger.error("Role not found: {}", roleTitle);
          return new ResourceNotFoundException(
              ApiErrorCodes.ROLE_NOT_FOUND, "Role not found: " + roleTitle);
        }))
      .collect(Collectors.toSet());
    user.setRoles(roles);

    User saved = userRepository.save(user);
    logger.info("User created successfully with ID: {}", saved.getId());
    return saved;
  }

  public User findByUsername(String username) {
    logger.debug("Finding user by username: {}", username);
    return userRepository.findByUsername(username)
      .orElseThrow(() -> {
        logger.error("User not found: {}", username);
        return new ResourceNotFoundException(ApiErrorCodes.USER_NOT_FOUND, "User not found");
      });
  }

  public User findById(Long id) {
    logger.debug("Finding user by ID: {}", id);
    return userRepository.findById(id)
      .orElseThrow(() -> {
        logger.error("User not found with ID: {}", id);
        return new ResourceNotFoundException(ApiErrorCodes.USER_NOT_FOUND, "User not found");
      });
  }

  public List<User> getAllUsers() {
    logger.debug("Fetching all users");
    return userRepository.findAll();
  }

  @Transactional(readOnly = true)
  public MeResponseDTO getMe(String username) {
    User user = userRepository.findByUsernameWithRolesAndPermissions(username)
        .orElseThrow(() -> {
          logger.error("User not found: {}", username);
          return new ResourceNotFoundException(ApiErrorCodes.USER_NOT_FOUND, "User not found");
        });
    Set<String> roleTitles = user.getRoles().stream().map(Role::getTitle).collect(Collectors.toSet());
    Set<String> permissions = user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .map(Permission::getPermission)
        .collect(Collectors.toSet());
    return new MeResponseDTO(
        user.getId(),
        user.getUsername(),
        user.isEnabled(),
        roleTitles,
        permissions);
  }

  @Transactional
  public User updateUser(Long id, UserDTO userDTO) {
    logger.info("Updating user ID: {}", id);

    User user = userRepository.findById(id).orElseThrow(() -> {
      logger.error("User not found with ID: {}", id);
      return new ResourceNotFoundException(ApiErrorCodes.USER_NOT_FOUND, "User not found");
    });

    // Обновляем username только если он изменился и не занят
    if (userDTO.getUsername() != null && !userDTO.getUsername().equals(user.getUsername())) {
      if (userRepository.existsByUsername(userDTO.getUsername())) {
        logger.error("Username already exists: {}", userDTO.getUsername());
        throw new BadRequestException(ApiErrorCodes.USERNAME_TAKEN, "Username already taken");
      }
      user.setUsername(userDTO.getUsername());
    }
    
    // Обновляем пароль только если он указан
    if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
      user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
    }
    
    // Обновляем роли (ровно одна роль)
    if (userDTO.getRoleTitles() != null) {
      if (userDTO.getRoleTitles().isEmpty()) {
        throw new BadRequestException(ApiErrorCodes.ROLE_REQUIRED, "Select one role");
      }
      if (userDTO.getRoleTitles().size() != 1) {
        throw new BadRequestException(ApiErrorCodes.ROLE_SINGLE_REQUIRED, "Exactly one role required");
      }
      Set<Role> roles = userDTO.getRoleTitles().stream()
        .map(roleTitle -> roleRepository.findByTitle(roleTitle)
          .orElseThrow(() -> {
            logger.error("Role not found: {}", roleTitle);
            return new ResourceNotFoundException(
                ApiErrorCodes.ROLE_NOT_FOUND, "Role not found: " + roleTitle);
          }))
        .collect(Collectors.toSet());
      user.setRoles(roles);
    }
    
    User saved = userRepository.save(user);
    logger.info("User updated successfully with ID: {}", id);
    return saved;
  }

  @Transactional
  public User deactivateUser(Long id) {
    logger.info("Deactivating user ID: {}", id);
    User user = findById(id);
    user.setEnabled(false);
    User saved = userRepository.save(user);
    logger.info("User deactivated successfully with ID: {}", id);
    return saved;
  }

  @Transactional
  public User activateUser(Long id) {
    logger.info("Activating user ID: {}", id);
    User user = findById(id);
    user.setEnabled(true);
    User saved = userRepository.save(user);
    logger.info("User activated successfully with ID: {}", id);
    return saved;
  }

  /**
   * Полное удаление учётной записи: снятие назначений с датчиков, затем удаление строки в БД.
   */
  @Transactional
  public void permanentlyDeleteUser(Long id) {
    logger.info("Permanently deleting user ID: {}", id);
    User user = userRepository.findById(id).orElseThrow(() -> {
      logger.error("User not found with ID: {}", id);
      return new ResourceNotFoundException(ApiErrorCodes.USER_NOT_FOUND, "User not found");
    });

    for (Sensor sensor : sensorRepository.findByAssignedToId(id)) {
      sensor.setAssignedTo(null);
      sensorRepository.save(sensor);
    }

    user.getRoles().clear();
    userRepository.delete(user);
    logger.info("User permanently deleted with ID: {}", id);
  }
}
