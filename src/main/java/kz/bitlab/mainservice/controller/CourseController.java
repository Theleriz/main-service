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
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.CourseCreateRequest;
import kz.bitlab.mainservice.dto.CourseDto;
import kz.bitlab.mainservice.dto.CourseUpdateRequest;
import kz.bitlab.mainservice.exception.ErrorResponse;
import kz.bitlab.mainservice.service.ChapterService;
import kz.bitlab.mainservice.service.CourseService;
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
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management: create, read, update and delete courses")
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a course", description = "Creates a new course and returns it with the generated ID")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Course created",
                    content = @Content(schema = @Schema(implementation = CourseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or malformed JSON",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CourseDto create(@Valid @RequestBody CourseCreateRequest request) {
        return courseService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course", description = "Updates the name and the description of a course")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course updated",
                    content = @Content(schema = @Schema(implementation = CourseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error, malformed JSON or invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CourseDto update(@Parameter(description = "Course ID", example = "1") @PathVariable Long id,
                            @Valid @RequestBody CourseUpdateRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a course", description = "Deletes a course together with all its chapters and lessons")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Course deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@Parameter(description = "Course ID", example = "1") @PathVariable Long id) {
        courseService.delete(id);
    }
}
