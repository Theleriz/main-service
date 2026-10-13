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
@Schema(description = "Course chapter")
public class ChapterDto {

    @Schema(description = "Chapter ID", example = "3")
    private Long id;

    @Schema(description = "Chapter name", example = "if-else conditions")
    private String name;

    @Schema(description = "Chapter description", example = "Making decisions in code with if, else if and else.")
    private String description;

    @Schema(description = "Sort order inside the course", example = "3")
    private Integer order;

    @Schema(description = "ID of the course the chapter belongs to", example = "1")
    private Long courseId;

    @Schema(description = "Creation time", example = "2026-10-13T10:15:30")
    private LocalDateTime createdTime;

    @Schema(description = "Last update time", example = "2026-10-13T10:15:30")
    private LocalDateTime updatedTime;
}
