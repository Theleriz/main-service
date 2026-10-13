package kz.bitlab.mainservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a course")
public class CourseUpdateRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Course name", example = "Java Backend Developer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Course description", example = "Java, Spring Boot, PostgreSQL and microservices.")
    private String description;
}
