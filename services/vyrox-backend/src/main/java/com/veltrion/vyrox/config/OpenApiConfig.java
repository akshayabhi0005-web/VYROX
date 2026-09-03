package com.veltrion.vyrox.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vyroxOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("VYROX Commerce Platform API")
                        .description("REST API for VYROX Smart Commerce Platform by Team VELTRION (Web & Android). Tagline: SHOP SMART. COMPARE BETTER. LIVE BETTER.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Team VELTRION")
                                .email("support@vyrox.com"))
                        .license(new License().name("Proprietary - VYROX")))
                .servers(List.of(
                        new Server().url("/").description("Current Server (Auto-detected Host/Proxy)"),
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("http://10.0.2.2:8080").description("Android Emulator Host Loopback")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
