package kz.bitlab.mainservice.controller;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.config.WithSecurityConfig;
import kz.bitlab.mainservice.dto.CourseCreateRequest;
import kz.bitlab.mainservice.dto.CourseDto;
import kz.bitlab.mainservice.dto.CourseUpdateRequest;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.service.ChapterService;
import kz.bitlab.mainservice.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static kz.bitlab.mainservice.TestData.COURSE_ID;
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

@WebMvcTest(CourseController.class)
@WithSecurityConfig
@WithMockUser(roles = "ADMIN")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private ChapterService chapterService;

    @Test
    void getAll_returns200AndCourses() throws Exception {
        when(courseService.getAll()).thenReturn(List.of(TestData.courseDto()));

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(COURSE_ID))
                .andExpect(jsonPath("$[0].name").value(TestData.COURSE_NAME));
    }

    @Test
    void getById_existingId_returns200() throws Exception {
        when(courseService.getById(COURSE_ID)).thenReturn(TestData.courseDto());

        mockMvc.perform(get("/api/v1/courses/{id}", COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(TestData.COURSE_NAME))
                .andExpect(jsonPath("$.description").value(TestData.COURSE_DESCRIPTION))
                .andExpect(jsonPath("$.createdTime").value("2026-10-13T10:15:30"));
    }

    @Test
    void getById_missingId_returns404WithMessage() throws Exception {
        when(courseService.getById(MISSING_ID)).thenThrow(new NotFoundException("Course with id 999 not found"));

        mockMvc.perform(get("/api/v1/courses/{id}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Course with id 999 not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/courses/999"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void getById_invalidId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/courses/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getChapters_existingCourse_returns200AndChapters() throws Exception {
        when(chapterService.getAllByCourseId(COURSE_ID)).thenReturn(List.of(TestData.chapterDto()));

        mockMvc.perform(get("/api/v1/courses/{id}/chapters", COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value(TestData.CHAPTER_NAME))
                .andExpect(jsonPath("$[0].courseId").value(COURSE_ID));
    }

    @Test
    void getChapters_missingCourse_returns404() throws Exception {
        when(chapterService.getAllByCourseId(MISSING_ID))
                .thenThrow(new NotFoundException("Course with id 999 not found"));

        mockMvc.perform(get("/api/v1/courses/{id}/chapters", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course with id 999 not found"));
    }

    @Test
    void create_validBody_returns201() throws Exception {
        when(courseService.create(any(CourseCreateRequest.class))).thenReturn(TestData.courseDto());

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Java Developer", "description": "A complete path to backend development."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(COURSE_ID))
                .andExpect(jsonPath("$.name").value(TestData.COURSE_NAME));
    }

    @Test
    void create_blankName_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "description": "A complete path to backend development."}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verify(courseService, never()).create(any());
    }

    @Test
    void create_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"));
    }

    @Test
    void update_validBody_returns200() throws Exception {
        CourseDto updated = TestData.courseDto();
        updated.setName("Java Backend Developer");
        when(courseService.update(eq(COURSE_ID), any(CourseUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/courses/{id}", COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Java Backend Developer", "description": "Java, Spring Boot and PostgreSQL."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java Backend Developer"));
    }

    @Test
    void update_missingId_returns404() throws Exception {
        when(courseService.update(eq(MISSING_ID), any(CourseUpdateRequest.class)))
                .thenThrow(new NotFoundException("Course with id 999 not found"));

        mockMvc.perform(put("/api/v1/courses/{id}", MISSING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Java Backend Developer"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course with id 999 not found"));
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/courses/{id}", COURSE_ID))
                .andExpect(status().isNoContent());

        verify(courseService).delete(COURSE_ID);
    }

    @Test
    void delete_missingId_returns404() throws Exception {
        doThrow(new NotFoundException("Course with id 999 not found")).when(courseService).delete(MISSING_ID);

        mockMvc.perform(delete("/api/v1/courses/{id}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course with id 999 not found"));
    }

    @Test
    void unexpectedError_returns500WithoutDetails() throws Exception {
        when(courseService.getAll()).thenThrow(new IllegalStateException("Connection refused: db:5432"));

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    @WithAnonymousUser
    void getAll_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/courses"));

        verify(courseService, never()).getAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAll_asUser_returns200() throws Exception {
        when(courseService.getAll()).thenReturn(List.of(TestData.courseDto()));

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_asUser_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Java Developer"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(courseService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void create_asTeacher_returns201() throws Exception {
        when(courseService.create(any(CourseCreateRequest.class))).thenReturn(TestData.courseDto());

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Java Developer"}
                                """))
                .andExpect(status().isCreated());
    }
}
