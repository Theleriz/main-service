package kz.bitlab.mainservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI mainServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("BITLAB LMS — Main Service")
                        .version("1.0.0")
                        .description("Main Service of the BITLAB ACADEMY LMS. Manages courses, chapters and lessons: "
                                + "a course consists of ordered chapters, a chapter consists of ordered lessons. "
                                + "All errors are returned in a unified ErrorResponse format. "
                                + "Users, roles and tokens are managed by Keycloak.")
                        .contact(new Contact()
                                .name("BITLAB ACADEMY")))
                // "Authorize" button in Swagger UI: paste the accessToken from /api/v1/auth/login
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
