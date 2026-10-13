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
import kz.bitlab.mainservice.dto.ChapterCreateRequest;
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.ChapterUpdateRequest;
import kz.bitlab.mainservice.dto.LessonDto;
import kz.bitlab.mainservice.exception.ErrorResponse;
import kz.bitlab.mainservice.service.ChapterService;
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
@RequestMapping("/api/v1/chapters")
@RequiredArgsConstructor
@Tag(name = "Chapters", description = "Chapter management: chapters belong to a course and are ordered inside it")
public class ChapterController {

    private final ChapterService chapterService;
    private final LessonService lessonService;

    @GetMapping
    @Operation(summary = "Get all chapters", description = "Returns chapters of all courses sorted by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of chapters",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChapterDto.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ChapterDto> getAll() {
        return chapterService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a chapter by ID", description = "Returns a single chapter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chapter found",
                    content = @Content(schema = @Schema(implementation = ChapterDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chapter not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ChapterDto getById(@Parameter(description = "Chapter ID", example = "1") @PathVariable Long id) {
        return chapterService.getById(id);
    }

    @GetMapping("/{id}/lessons")
    @Operation(summary = "Get lessons of a chapter", description = "Returns lessons of the chapter sorted by their order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of lessons",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LessonDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chapter not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<LessonDto> getLessons(@Parameter(description = "Chapter ID", example = "1") @PathVariable Long id) {
        return lessonService.getAllByChapterId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a chapter", description = "Creates a new chapter in an existing course")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chapter created",
                    content = @Content(schema = @Schema(implementation = ChapterDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or malformed JSON",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ChapterDto create(@Valid @RequestBody ChapterCreateRequest request) {
        return chapterService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a chapter", description = "Updates the name, the description and the order of a chapter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chapter updated",
                    content = @Content(schema = @Schema(implementation = ChapterDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error, malformed JSON or invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chapter not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ChapterDto update(@Parameter(description = "Chapter ID", example = "1") @PathVariable Long id,
                             @Valid @RequestBody ChapterUpdateRequest request) {
        return chapterService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a chapter", description = "Deletes a chapter together with all its lessons")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chapter deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chapter not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@Parameter(description = "Chapter ID", example = "1") @PathVariable Long id) {
        chapterService.delete(id);
    }
}
