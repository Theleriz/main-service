package kz.bitlab.mainservice.controller;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.config.WithSecurityConfig;
import kz.bitlab.mainservice.dto.FileDownload;
import kz.bitlab.mainservice.exception.BadRequestException;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.service.AttachmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static kz.bitlab.mainservice.TestData.ATTACHMENT_ID;
import static kz.bitlab.mainservice.TestData.ATTACHMENT_NAME;
import static kz.bitlab.mainservice.TestData.LESSON_ID;
import static kz.bitlab.mainservice.TestData.MISSING_ID;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttachmentController.class)
@WithSecurityConfig
class AttachmentControllerTest {

    private static final byte[] FILE_CONTENT = "%PDF-1.7 cheatsheet".getBytes(StandardCharsets.UTF_8);
    private static final MockMultipartFile FILE =
            new MockMultipartFile("file", ATTACHMENT_NAME, "application/pdf", FILE_CONTENT);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttachmentService attachmentService;

    @Test
    @WithMockUser(roles = "TEACHER")
    void upload_asTeacher_returns201() throws Exception {
        when(attachmentService.upload(eq(LESSON_ID), any())).thenReturn(TestData.attachmentDto());

        mockMvc.perform(multipart("/attachment/upload").file(FILE).param("lessonId", String.valueOf(LESSON_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ATTACHMENT_ID))
                .andExpect(jsonPath("$.name").value(ATTACHMENT_NAME))
                .andExpect(jsonPath("$.url").value(TestData.ATTACHMENT_URL))
                .andExpect(jsonPath("$.lessonId").value(LESSON_ID));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upload_asAdmin_returns201() throws Exception {
        when(attachmentService.upload(eq(LESSON_ID), any())).thenReturn(TestData.attachmentDto());

        mockMvc.perform(multipart("/attachment/upload").file(FILE).param("lessonId", String.valueOf(LESSON_ID)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void upload_asUser_returns403() throws Exception {
        mockMvc.perform(multipart("/attachment/upload").file(FILE).param("lessonId", String.valueOf(LESSON_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(attachmentService, never()).upload(any(), any());
    }

    @Test
    @WithAnonymousUser
    void upload_withoutToken_returns401() throws Exception {
        mockMvc.perform(multipart("/attachment/upload").file(FILE).param("lessonId", String.valueOf(LESSON_ID)))
                .andExpect(status().isUnauthorized());

        verify(attachmentService, never()).upload(any(), any());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void upload_withoutFile_returns400() throws Exception {
        mockMvc.perform(multipart("/attachment/upload").param("lessonId", String.valueOf(LESSON_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Part 'file' is required"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void upload_withoutLessonId_returns400() throws Exception {
        mockMvc.perform(multipart("/attachment/upload").file(FILE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'lessonId' is required"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void upload_emptyFile_returns400() throws Exception {
        when(attachmentService.upload(eq(LESSON_ID), any())).thenThrow(new BadRequestException("File must not be empty"));

        mockMvc.perform(multipart("/attachment/upload")
                        .file(new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]))
                        .param("lessonId", String.valueOf(LESSON_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File must not be empty"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void upload_missingLesson_returns404() throws Exception {
        when(attachmentService.upload(eq(MISSING_ID), any()))
                .thenThrow(new NotFoundException("Lesson with id 999 not found"));

        mockMvc.perform(multipart("/attachment/upload").file(FILE).param("lessonId", String.valueOf(MISSING_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Lesson with id 999 not found"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void download_asUser_returnsFileWithHeaders() throws Exception {
        when(attachmentService.download(eq(ATTACHMENT_ID), any())).thenReturn(new FileDownload(
                "шпаргалка if-else.pdf", "application/pdf", FILE_CONTENT.length,
                new ByteArrayInputStream(FILE_CONTENT)));

        mockMvc.perform(get("/attachment/download/{id}", ATTACHMENT_ID))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().longValue("Content-Length", FILE_CONTENT.length))
                // Non-ASCII names are sent as RFC 5987 filename*
                .andExpect(header().string("Content-Disposition", startsWith("attachment;")))
                .andExpect(header().string("Content-Disposition", containsString(
                        "filename*=UTF-8''%D1%88%D0%BF%D0%B0%D1%80%D0%B3%D0%B0%D0%BB%D0%BA%D0%B0%20if-else.pdf")))
                .andExpect(content().bytes(FILE_CONTENT));
    }

    @Test
    @WithMockUser(roles = "USER")
    void download_noCourseAccess_returns403() throws Exception {
        when(attachmentService.download(eq(ATTACHMENT_ID), any()))
                .thenThrow(new AccessDeniedException("No access to course with id 1"));

        mockMvc.perform(get("/attachment/download/{id}", ATTACHMENT_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No access to course with id 1"));
    }

    @Test
    void download_userWithoutLmsRole_returns403() throws Exception {
        // Authenticated in Keycloak, but has none of ROLE_ADMIN, ROLE_TEACHER, ROLE_USER
        mockMvc.perform(get("/attachment/download/{id}", ATTACHMENT_ID)
                        .with(user("guest").authorities(List.of())))
                .andExpect(status().isForbidden());

        verify(attachmentService, never()).download(any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void download_missingAttachment_returns404() throws Exception {
        when(attachmentService.download(eq(MISSING_ID), any()))
                .thenThrow(new NotFoundException("Attachment with id 999 not found"));

        mockMvc.perform(get("/attachment/download/{id}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Attachment with id 999 not found"));
    }

    @Test
    @WithAnonymousUser
    void download_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/attachment/download/{id}", ATTACHMENT_ID))
                .andExpect(status().isUnauthorized());
    }
}
