package com.jeffry.ecommerce.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do OpenAPI/Swagger. A documentação interativa fica disponível
 * em /swagger-ui.html, com suporte a autenticação via JWT (botão "Authorize").
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Ecommerce API",
                version = "v1",
                description = "API REST para uma loja virtual: catálogo de produtos, categorias, carrinho, pedidos e autenticação via JWT.",
                contact = @Contact(name = "Jeffry Smith")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
