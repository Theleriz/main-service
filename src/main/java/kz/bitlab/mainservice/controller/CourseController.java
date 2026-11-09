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
import kz.bitlab.mainservice.dto.CourseDto;
import kz.bitlab.mainservice.exception.ErrorResponse;
import kz.bitlab.mainservice.service.ChapterService;
import kz.bitlab.mainservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Reading courses. Courses are created, updated and deleted through /course (ROLE_ADMIN)")
public class CourseController {

    private final CourseService courseService;
    private final ChapterService chapterService;

    @GetMapping
    @Operation(summary = "Get all courses", description = "Returns all courses sorted by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of courses",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CourseDto.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<CourseDto> getAll() {
        return courseService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a course by ID", description = "Returns a single course")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course found",
                    content = @Content(schema = @Schema(implementation = CourseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CourseDto getById(@Parameter(description = "Course ID", example = "1") @PathVariable Long id) {
        return courseService.getById(id);
    }

    @GetMapping("/{id}/chapters")
    @Operation(summary = "Get chapters of a course", description = "Returns chapters of the course sorted by their order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of chapters",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChapterDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ChapterDto> getChapters(@Parameter(description = "Course ID", example = "1") @PathVariable Long id) {
        return chapterService.getAllByCourseId(id);
    }
}
