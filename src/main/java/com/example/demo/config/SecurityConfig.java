package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Отключаем CSRF для stateless API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/incidents/*/upload").permitAll() // Thymeleaf форма загрузки
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll() // Статика фото/PDF (img/ссылки без заголовка Authorization)
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAuthority("user.read")
                .requestMatchers(HttpMethod.POST, "/api/users/**").hasAuthority("user.write")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority("user.write")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("user.write")
                .requestMatchers(HttpMethod.GET, "/api/sensors/**").hasAuthority("sensor.read")
                .requestMatchers(HttpMethod.POST, "/api/sensors/**").hasAuthority("sensor.write")
                .requestMatchers(HttpMethod.PUT, "/api/sensors/**").hasAuthority("sensor.write")
                .requestMatchers(HttpMethod.DELETE, "/api/sensors/**").hasAuthority("sensor.write")
                .requestMatchers(HttpMethod.GET, "/api/roles/**", "/api/permissions/**").hasAuthority("user.read")
                .requestMatchers(HttpMethod.GET, "/api/incidents/**").hasAuthority("alert.read")
                .requestMatchers(HttpMethod.POST, "/api/incidents/**").hasAuthority("alert.write")
                .requestMatchers(HttpMethod.PUT, "/api/incidents/**").hasAuthority("alert.write")
                .requestMatchers(HttpMethod.DELETE, "/api/incidents/*/photos").hasAuthority("alert.write")
                .requestMatchers(HttpMethod.DELETE, "/api/incidents/*/report").hasAuthority("alert.write")
                .requestMatchers(HttpMethod.DELETE, "/api/incidents/*").hasAuthority("alert.delete")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                // Нет аутентификации - 401
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Требуется аутентификация\"}");
                })
                // Аутентифицирован, но прав нет - 403
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Недостаточно прав\"}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:9000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
