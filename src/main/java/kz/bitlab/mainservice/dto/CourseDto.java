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
@Schema(description = "Course")
public class CourseDto {

    @Schema(description = "Course ID", example = "1")
    private Long id;

    @Schema(description = "Course name", example = "Java Developer")
    private String name;

    @Schema(description = "Course description", example = "A complete path from the first line of Java code to backend development.")
    private String description;

    @Schema(description = "Creation time", example = "2026-10-13T10:15:30")
    private LocalDateTime createdTime;

    @Schema(description = "Last update time", example = "2026-10-13T10:15:30")
    private LocalDateTime updatedTime;
}
