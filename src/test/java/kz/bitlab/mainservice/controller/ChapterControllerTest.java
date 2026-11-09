package kz.bitlab.mainservice.controller;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.config.WithSecurityConfig;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.service.ChapterService;
import kz.bitlab.mainservice.service.LessonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static kz.bitlab.mainservice.TestData.CHAPTER_ID;
import static kz.bitlab.mainservice.TestData.COURSE_ID;
import static kz.bitlab.mainservice.TestData.MISSING_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChapterController.class)
@WithSecurityConfig
@WithMockUser(roles = "ADMIN")
class ChapterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChapterService chapterService;

    @MockitoBean
    private LessonService lessonService;

    @Test
    void getAll_returns200AndChapters() throws Exception {
        when(chapterService.getAll()).thenReturn(List.of(TestData.chapterDto()));

        mockMvc.perform(get("/api/v1/chapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value(TestData.CHAPTER_NAME));
    }

    @Test
    void getById_existingId_returns200() throws Exception {
        when(chapterService.getById(CHAPTER_ID)).thenReturn(TestData.chapterDto());

        mockMvc.perform(get("/api/v1/chapters/{id}", CHAPTER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CHAPTER_ID))
                .andExpect(jsonPath("$.order").value(3))
                .andExpect(jsonPath("$.courseId").value(COURSE_ID));
    }

    @Test
    void getById_missingId_returns404WithMessage() throws Exception {
        when(chapterService.getById(MISSING_ID)).thenThrow(new NotFoundException("Chapter with id 999 not found"));

        mockMvc.perform(get("/api/v1/chapters/{id}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Chapter with id 999 not found"));
    }

    @Test
    void getById_invalidId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/chapters/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLessons_existingChapter_returns200AndLessons() throws Exception {
        when(lessonService.getAllByChapterId(CHAPTER_ID)).thenReturn(List.of(TestData.lessonDto()));

        mockMvc.perform(get("/api/v1/chapters/{id}/lessons", CHAPTER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value(TestData.LESSON_NAME))
                .andExpect(jsonPath("$[0].chapterId").value(CHAPTER_ID));
    }

    @Test
    void getLessons_missingChapter_returns404() throws Exception {
        when(lessonService.getAllByChapterId(MISSING_ID))
                .thenThrow(new NotFoundException("Chapter with id 999 not found"));

        mockMvc.perform(get("/api/v1/chapters/{id}/lessons", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Chapter with id 999 not found"));
    }
}
