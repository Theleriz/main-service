package kz.bitlab.mainservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mainServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("BITLAB LMS — Main Service")
                        .version("1.0.0")
                        .description("Main Service of the BITLAB ACADEMY LMS. Manages courses, chapters and lessons: "
                                + "a course consists of ordered chapters, a chapter consists of ordered lessons. "
                                + "All errors are returned in a unified ErrorResponse format.")
                        .contact(new Contact()
                                .name("BITLAB ACADEMY")));
    }
}
