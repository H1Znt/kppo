package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fire Safety Monitoring API")
                        .version("1.0.0")
                        .description("API для системы мониторинга пожарной безопасности в учебном корпусе")
                        .contact(new Contact()
                                .name("Support")
                                .email("support@example.com")))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("jwtToken")));
    }
}
