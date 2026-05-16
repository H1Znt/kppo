package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UUID — случайный токен
    @Column(nullable = false, unique = true)
    private String tokenValue;

    // Для очистки старых записей из БД
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // true = токен отозван
    @Column(nullable = false)
    private boolean revoked = false;

    // Какому пользователю принадлежит токен
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
