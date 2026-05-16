package com.example.demo.repository;

import com.example.demo.model.Token;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    // Найти токен по его значению (JWT строке)
    Optional<Token> findByTokenValue(String tokenValue);

    // Все токены конкретного пользователя
    List<Token> findByUser(User user);

    // Удаляет все просроченные токены
    @Modifying
    @Query("DELETE FROM Token t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
