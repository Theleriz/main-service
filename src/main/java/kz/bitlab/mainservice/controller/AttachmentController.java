package kz.bitlab.mainservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.bitlab.mainservice.dto.AttachmentDto;
import kz.bitlab.mainservice.dto.FileDownload;
import kz.bitlab.mainservice.exception.ErrorResponse;
import kz.bitlab.mainservice.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/attachment")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "Uploading files to lessons and downloading them (files are stored in MinIO)")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Upload a file to a lesson (ROLE_ADMIN, ROLE_TEACHER)",
            description = "Stores the file in MinIO and attaches it to the lesson. A lesson can have many attachments")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "File uploaded",
                    content = @Content(schema = @Schema(implementation = AttachmentDto.class))),
            @ApiResponse(responseCode = "400", description = "Empty file, missing file or lessonId",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not an admin or a teacher",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lesson not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "File is larger than 50 MB",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public AttachmentDto upload(@Parameter(description = "Lesson ID", example = "1") @RequestParam Long lessonId,
                                @Parameter(description = "File to upload") @RequestPart("file") MultipartFile file) {
        return attachmentService.upload(lessonId, file);
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "Download an attachment",
            description = "Checks the access level to the course of the lesson and returns the file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File content",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No access to the course",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Attachment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InputStreamResource> download(
            @Parameter(description = "Attachment ID", example = "1") @PathVariable Long id,
            Authentication authentication) {
        FileDownload file = attachmentService.download(id, authentication);
        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header("Content-Disposition", ContentDisposition.attachment()
                        .filename(file.name(), StandardCharsets.UTF_8)
                        .build()
                        .toString());
        if (file.size() >= 0) {
            response.contentLength(file.size());
        }
        return response.body(new InputStreamResource(file.content()));
    }
}
