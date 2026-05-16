package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme cookieScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("access_token");  // браузер отправляет автоматически

        // Применять эту схему ко всем операциям глобально
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("cookieAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("Fire Safety Monitoring API")
                        .version("1.0.0")
                        .description("API для системы мониторинга пожарной безопасности. " +
                                "Для авторизации: выполните POST /api/auth/login — браузер сохранит куку access_token автоматически.")
                        .contact(new Contact()
                                .name("Support")
                                .email("support@example.com")))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", cookieScheme))
                .security(List.of(securityRequirement)); // применяем глобально
    }
}
