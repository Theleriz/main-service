package kz.bitlab.mainservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a lesson")
public class LessonUpdateRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Lesson name", example = "Lecture for if-else conditions", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Lesson description", example = "Theory: conditional statements, ternary operator and switch.")
    private String description;

    @Schema(description = "Lesson content (HTML from a rich text editor)", example = "<h2>Making decisions with if-else</h2><p>...</p>")
    private String content;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Sort order inside the chapter", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer order;
}
