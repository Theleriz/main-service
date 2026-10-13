package kz.bitlab.mainservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Unified error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP status reason", example = "Not Found")
    private String error;

    @Schema(description = "Human-readable error message", example = "Course with id 42 not found")
    private String message;

    @Schema(description = "Request path", example = "/api/v1/courses/42")
    private String path;

    @Schema(description = "Time of the error", example = "2026-10-13T10:15:30")
    private LocalDateTime timestamp;

    @Schema(description = "Validation errors by field (only for 400 validation errors)", example = "{\"name\": \"must not be blank\"}")
    private Map<String, String> fieldErrors;
}
