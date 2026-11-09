package kz.bitlab.mainservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.LessonDto;
import kz.bitlab.mainservice.exception.ErrorResponse;
import kz.bitlab.mainservice.service.ChapterService;
import kz.bitlab.mainservice.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chapters")
@RequiredArgsConstructor
@Tag(name = "Chapters", description = "Reading chapters: chapters belong to a course and are ordered inside it. "
        + "Chapters are created, updated and deleted through /chapter (ROLE_ADMIN)")
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
}
