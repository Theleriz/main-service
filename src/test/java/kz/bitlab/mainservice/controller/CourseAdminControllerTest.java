package kz.bitlab.mainservice.controller;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.config.WithSecurityConfig;
import kz.bitlab.mainservice.dto.CourseCreateRequest;
import kz.bitlab.mainservice.dto.CourseDto;
import kz.bitlab.mainservice.dto.CourseUpdateRequest;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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

@WebMvcTest(CourseAdminController.class)
@WithSecurityConfig
@WithMockUser(roles = "ADMIN")
class CourseAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @Test
    void create_validBody_returns201() throws Exception {
        when(courseService.create(any(CourseCreateRequest.class))).thenReturn(TestData.courseDto());

        mockMvc.perform(post("/course")
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
        mockMvc.perform(post("/course")
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
        mockMvc.perform(post("/course")
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

        mockMvc.perform(put("/course/{id}", COURSE_ID)
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

        mockMvc.perform(put("/course/{id}", MISSING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Java Backend Developer"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course with id 999 not found"));
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/course/{id}", COURSE_ID))
                .andExpect(status().isNoContent());

        verify(courseService).delete(COURSE_ID);
    }

    @Test
    void delete_missingId_returns404() throws Exception {
        doThrow(new NotFoundException("Course with id 999 not found")).when(courseService).delete(MISSING_ID);

        mockMvc.perform(delete("/course/{id}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course with id 999 not found"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void create_asTeacher_returns403() throws Exception {
        mockMvc.perform(post("/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Java Developer"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(courseService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void update_asUser_returns403() throws Exception {
        mockMvc.perform(put("/course/{id}", COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Java Backend Developer"}
                                """))
                .andExpect(status().isForbidden());

        verify(courseService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void delete_asTeacher_returns403() throws Exception {
        mockMvc.perform(delete("/course/{id}", COURSE_ID))
                .andExpect(status().isForbidden());

        verify(courseService, never()).delete(any());
    }

    @Test
    @WithAnonymousUser
    void create_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Java Developer"}
                                """))
                .andExpect(status().isUnauthorized());

        verify(courseService, never()).create(any());
    }
}
