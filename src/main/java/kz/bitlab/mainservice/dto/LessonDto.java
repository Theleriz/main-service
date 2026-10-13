package kz.bitlab.mainservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chapter lesson")
public class LessonDto {

    @Schema(description = "Lesson ID", example = "5")
    private Long id;

    @Schema(description = "Lesson name", example = "Lecture for if-else conditions")
    private String name;

    @Schema(description = "Lesson description", example = "Theory: conditional statements and logical operators.")
    private String description;

    @Schema(description = "Lesson content (HTML from a rich text editor)", example = "<h2>Making decisions with if-else</h2><p>...</p>")
    private String content;

    @Schema(description = "Sort order inside the chapter", example = "1")
    private Integer order;

    @Schema(description = "ID of the chapter the lesson belongs to", example = "3")
    private Long chapterId;

    @Schema(description = "Creation time", example = "2026-10-13T10:15:30")
    private LocalDateTime createdTime;

    @Schema(description = "Last update time", example = "2026-10-13T10:15:30")
    private LocalDateTime updatedTime;
}
