package kz.bitlab.mainservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.bitlab.mainservice.dto.LessonCreateRequest;
import kz.bitlab.mainservice.dto.LessonDto;
import kz.bitlab.mainservice.dto.LessonUpdateRequest;
import kz.bitlab.mainservice.exception.ErrorResponse;
import kz.bitlab.mainservice.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "Lesson management: lessons belong to a chapter and contain HTML content")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    @Operation(summary = "Get all lessons", description = "Returns lessons of all chapters sorted by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of lessons",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LessonDto.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<LessonDto> getAll() {
        return lessonService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a lesson by ID", description = "Returns a single lesson with its HTML content")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson found",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lesson not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public LessonDto getById(@Parameter(description = "Lesson ID", example = "1") @PathVariable Long id) {
        return lessonService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a lesson", description = "Creates a new lesson in an existing chapter")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lesson created",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or malformed JSON",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chapter not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public LessonDto create(@Valid @RequestBody LessonCreateRequest request) {
        return lessonService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a lesson", description = "Updates the name, the description, the content and the order of a lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson updated",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error, malformed JSON or invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lesson not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public LessonDto update(@Parameter(description = "Lesson ID", example = "1") @PathVariable Long id,
                            @Valid @RequestBody LessonUpdateRequest request) {
        return lessonService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a lesson", description = "Deletes a lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lesson deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lesson not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@Parameter(description = "Lesson ID", example = "1") @PathVariable Long id) {
        lessonService.delete(id);
    }
}
