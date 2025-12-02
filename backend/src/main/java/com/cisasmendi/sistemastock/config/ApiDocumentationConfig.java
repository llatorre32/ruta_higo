package com.cisasmendi.sistemastock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class ApiDocumentationConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Stock API")
                        .description("API para gestión de inventario y stock con sistema de autenticación JWT. " +
                                   "Permite crear, actualizar, gestionar productos y controlar el stock " +
                                   "con sistema de categorías, ubicaciones, movimientos, reportes y gestión de usuarios.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("")
                                .email(""))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT para autenticación. Formato: Bearer <token>")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}