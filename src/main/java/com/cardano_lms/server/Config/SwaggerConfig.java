package com.cardano_lms.server.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("LMS Backend API")
                                                .description("Learning Management System Backend API Documentation")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Cardano2VN Team")
                                                                .email("support@cardano2vn.com")
                                                                .url("https://cardano2vn.com"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                                .servers(List.of(
                                                new Server().url("http://localhost:8080")
                                                                .description("Development Server"),
                                                new Server().url("https://lms-backend-0-0-1.onrender.com")
                                                                .description("Production Server")));
        }

        @Bean
        public GroupedOpenApi lmsApiGroup() {
                return GroupedOpenApi.builder()
                                .group("lms")
                                .pathsToMatch("/api/**")
                                .build();
        }
}
