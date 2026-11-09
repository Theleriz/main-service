package kz.bitlab.mainservice.controller;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.config.WithSecurityConfig;
import kz.bitlab.mainservice.dto.LessonCreateRequest;
import kz.bitlab.mainservice.dto.LessonDto;
import kz.bitlab.mainservice.dto.LessonUpdateRequest;
import kz.bitlab.mainservice.dto.AttachmentDto;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.service.AttachmentService;
import kz.bitlab.mainservice.service.LessonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static kz.bitlab.mainservice.TestData.CHAPTER_ID;
import static kz.bitlab.mainservice.TestData.LESSON_ID;
import static kz.bitlab.mainservice.TestData.MISSING_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LessonController.class)
@WithSecurityConfig
@WithMockUser(roles = "ADMIN")
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LessonService lessonService;

    @MockitoBean
    private AttachmentService attachmentService;

    @Test
    void getAll_returns200AndLessons() throws Exception {
        when(lessonService.getAll()).thenReturn(List.of(TestData.lessonDto()));

        mockMvc.perform(get("/api/v1/lessons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value(TestData.LESSON_NAME));
    }

    @Test
    void getById_existingId_returns200WithContent() throws Exception {
        when(lessonService.getById(LESSON_ID)).thenReturn(TestData.lessonDto());

        mockMvc.perform(get("/api/v1/lessons/{id}", LESSON_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LESSON_ID))
                .andExpect(jsonPath("$.content").value(TestData.LESSON_CONTENT))
                .andExpect(jsonPath("$.chapterId").value(CHAPTER_ID));
    }

    @Test
    void getById_missingId_returns404WithMessage() throws Exception {
        when(lessonService.getById(MISSING_ID)).thenThrow(new NotFoundException("Lesson with id 999 not found"));

        mockMvc.perform(get("/api/v1/lessons/{id}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Lesson with id 999 not found"));
    }

    @Test
    void getById_invalidId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_validBody_returns201() throws Exception {
        when(lessonService.create(any(LessonCreateRequest.class))).thenReturn(TestData.lessonDto());

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lecture for if-else conditions",
                                  "description": "Theory: conditional statements.",
                                  "content": "<h2>Making decisions with if-else</h2>",
                                  "order": 1,
                                  "chapterId": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(LESSON_ID))
                .andExpect(jsonPath("$.chapterId").value(CHAPTER_ID));
    }

    @Test
    void create_blankNameAndMissingChapter_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "   ", "order": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.chapterId").exists());

        verify(lessonService, never()).create(any());
    }

    @Test
    void create_missingChapter_returns404() throws Exception {
        when(lessonService.create(any(LessonCreateRequest.class)))
                .thenThrow(new NotFoundException("Chapter with id 999 not found"));

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Lecture for if-else conditions", "order": 1, "chapterId": 999}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Chapter with id 999 not found"));
    }

    @Test
    void update_validBody_returns200() throws Exception {
        LessonDto updated = TestData.lessonDto();
        updated.setDescription("Theory: conditional statements, ternary operator and switch.");
        when(lessonService.update(eq(LESSON_ID), any(LessonUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/lessons/{id}", LESSON_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lecture for if-else conditions",
                                  "description": "Theory: conditional statements, ternary operator and switch.",
                                  "order": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Theory: conditional statements, ternary operator and switch."));
    }

    @Test
    void update_missingId_returns404() throws Exception {
        when(lessonService.update(eq(MISSING_ID), any(LessonUpdateRequest.class)))
                .thenThrow(new NotFoundException("Lesson with id 999 not found"));

        mockMvc.perform(put("/api/v1/lessons/{id}", MISSING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Lecture for if-else conditions", "order": 1}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/lessons/{id}", LESSON_ID))
                .andExpect(status().isNoContent());

        verify(lessonService).delete(LESSON_ID);
    }

    @Test
    void delete_missingId_returns404() throws Exception {
        doThrow(new NotFoundException("Lesson with id 999 not found")).when(lessonService).delete(MISSING_ID);

        mockMvc.perform(delete("/api/v1/lessons/{id}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Lesson with id 999 not found"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAttachments_asUser_returns200() throws Exception {
        when(attachmentService.getAllByLessonId(LESSON_ID)).thenReturn(List.of(AttachmentDto.builder()
                .id(1L).name("cheatsheet.pdf").url("lessons/5/file.pdf").lessonId(LESSON_ID).build()));

        mockMvc.perform(get("/api/v1/lessons/{id}/attachments", LESSON_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("cheatsheet.pdf"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_asUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/lessons/{id}", LESSON_ID))
                .andExpect(status().isForbidden());

        verify(lessonService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void delete_asTeacher_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/lessons/{id}", LESSON_ID))
                .andExpect(status().isNoContent());
    }
}
