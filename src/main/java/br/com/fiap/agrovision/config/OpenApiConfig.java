package br.com.fiap.agrovision.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AgroVision API",
                version = "2.0.0",
                description = "API REST para gestão de fazendas — usuários, plantações, safras, insumos e relatórios. " +
                              "Projeto desenvolvido para a Global Solution 2026/1 - FIAP. " +
                              "Autenticação via CPF + senha (JWT Bearer).",
                contact = @Contact(name = "Equipe AgroVision", email = "agrovision@fiap.com.br")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Autenticação via JWT. Faça login em /api/auth/login com CPF e senha.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {}
