package kz.bitlab.mainservice.controller;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.config.WithSecurityConfig;
import kz.bitlab.mainservice.dto.ChapterCreateRequest;
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.ChapterUpdateRequest;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.service.ChapterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static kz.bitlab.mainservice.TestData.CHAPTER_ID;
import static kz.bitlab.mainservice.TestData.COURSE_ID;
import static kz.bitlab.mainservice.TestData.MISSING_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChapterAdminController.class)
@WithSecurityConfig
@WithMockUser(roles = "ADMIN")
class ChapterAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChapterService chapterService;

    @Test
    void create_validBody_returns201() throws Exception {
        when(chapterService.create(any(ChapterCreateRequest.class))).thenReturn(TestData.chapterDto());

        mockMvc.perform(post("/chapter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "if-else conditions", "description": "Branching logic.", "order": 3, "courseId": 1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CHAPTER_ID))
                .andExpect(jsonPath("$.courseId").value(COURSE_ID));
    }

    @Test
    void create_missingCourseIdAndOrder_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/chapter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "if-else conditions"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.courseId").exists())
                .andExpect(jsonPath("$.fieldErrors.order").exists());

        verify(chapterService, never()).create(any());
    }

    @Test
    void create_missingCourse_returns404() throws Exception {
        when(chapterService.create(any(ChapterCreateRequest.class)))
                .thenThrow(new NotFoundException("Course with id 999 not found"));

        mockMvc.perform(post("/chapter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "if-else conditions", "order": 3, "courseId": 999}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course with id 999 not found"));
    }

    @Test
    void update_validBody_returns200() throws Exception {
        ChapterDto updated = TestData.chapterDto();
        updated.setName("Conditional statements: if, else if, else");
        when(chapterService.update(eq(CHAPTER_ID), any(ChapterUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/chapter/{id}", CHAPTER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Conditional statements: if, else if, else", "order": 3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conditional statements: if, else if, else"));
    }

    @Test
    void update_missingId_returns404() throws Exception {
        when(chapterService.update(eq(MISSING_ID), any(ChapterUpdateRequest.class)))
                .thenThrow(new NotFoundException("Chapter with id 999 not found"));

        mockMvc.perform(put("/chapter/{id}", MISSING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Conditional statements", "order": 3}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/chapter/{id}", CHAPTER_ID))
                .andExpect(status().isNoContent());

        verify(chapterService).delete(CHAPTER_ID);
    }

    @Test
    void delete_missingId_returns404() throws Exception {
        doThrow(new NotFoundException("Chapter with id 999 not found")).when(chapterService).delete(MISSING_ID);

        mockMvc.perform(delete("/chapter/{id}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Chapter with id 999 not found"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void create_asTeacher_returns403() throws Exception {
        mockMvc.perform(post("/chapter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "if-else", "order": 1, "courseId": 1}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(chapterService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void update_asUser_returns403() throws Exception {
        mockMvc.perform(put("/chapter/{id}", CHAPTER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "if-else", "order": 1}
                                """))
                .andExpect(status().isForbidden());

        verify(chapterService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void delete_asTeacher_returns403() throws Exception {
        mockMvc.perform(delete("/chapter/{id}", CHAPTER_ID))
                .andExpect(status().isForbidden());

        verify(chapterService, never()).delete(any());
    }

    @Test
    @WithAnonymousUser
    void delete_withoutToken_returns401() throws Exception {
        mockMvc.perform(delete("/chapter/{id}", CHAPTER_ID))
                .andExpect(status().isUnauthorized());

        verify(chapterService, never()).delete(any());
    }
}
