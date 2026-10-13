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
@Schema(description = "Request to update a chapter")
public class ChapterUpdateRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Chapter name", example = "Conditional statements: if, else if, else", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Chapter description", example = "Branching logic, logical operators and switch.")
    private String description;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Sort order inside the course", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer order;
}
