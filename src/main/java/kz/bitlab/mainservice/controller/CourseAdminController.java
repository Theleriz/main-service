package kz.bitlab.mainservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.bitlab.mainservice.dto.CourseCreateRequest;
import kz.bitlab.mainservice.dto.CourseDto;
import kz.bitlab.mainservice.dto.CourseUpdateRequest;
import kz.bitlab.mainservice.exception.ErrorResponse;
import kz.bitlab.mainservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create, update and delete courses. Only ROLE_ADMIN: restricted in SecurityConfig
 * and once more with @PreAuthorize.
 */
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Courses (admin)", description = "Creating, updating and deleting courses. Only ROLE_ADMIN")
public class CourseAdminController {

    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a course", description = "Creates a new course and returns it with the generated ID")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Course created",
                    content = @Content(schema = @Schema(implementation = CourseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or malformed JSON",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not an admin",
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
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not an admin",
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
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@Parameter(description = "Course ID", example = "1") @PathVariable Long id) {
        courseService.delete(id);
    }
}
