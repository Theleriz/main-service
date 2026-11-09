package kz.bitlab.mainservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.bitlab.mainservice.dto.ChapterCreateRequest;
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.ChapterUpdateRequest;
import kz.bitlab.mainservice.exception.ErrorResponse;
import kz.bitlab.mainservice.service.ChapterService;
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
 * Create, update and delete chapters. Only ROLE_ADMIN: restricted in SecurityConfig
 * and once more with @PreAuthorize.
 */
@RestController
@RequestMapping("/chapter")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Chapters (admin)", description = "Creating, updating and deleting chapters. Only ROLE_ADMIN")
public class ChapterAdminController {

    private final ChapterService chapterService;

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
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not an admin",
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
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not an admin",
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
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@Parameter(description = "Chapter ID", example = "1") @PathVariable Long id) {
        chapterService.delete(id);
    }
}
