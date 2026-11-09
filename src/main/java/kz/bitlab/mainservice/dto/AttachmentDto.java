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
@Schema(description = "File attached to a lesson, download it with GET /attachment/download/{id}")
public class AttachmentDto {

    @Schema(description = "Attachment ID", example = "1")
    private Long id;

    @Schema(description = "Original file name", example = "if-else-cheatsheet.pdf")
    private String name;

    @Schema(description = "Object name of the file in the MinIO bucket",
            example = "lessons/5/3f2b8c1e-6d4a-4f1b-9a7e-2c5d8e9f0a1b.pdf")
    private String url;

    @Schema(description = "ID of the lesson the file is attached to", example = "5")
    private Long lessonId;

    @Schema(description = "Upload time", example = "2026-11-09T10:15:30")
    private LocalDateTime createdTime;
}
