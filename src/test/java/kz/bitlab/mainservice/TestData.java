package kz.bitlab.mainservice;

import kz.bitlab.mainservice.dto.ChapterCreateRequest;
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.ChapterUpdateRequest;
import kz.bitlab.mainservice.dto.CourseCreateRequest;
import kz.bitlab.mainservice.dto.CourseDto;
import kz.bitlab.mainservice.dto.CourseUpdateRequest;
import kz.bitlab.mainservice.dto.LessonCreateRequest;
import kz.bitlab.mainservice.dto.LessonDto;
import kz.bitlab.mainservice.dto.LessonUpdateRequest;
import kz.bitlab.mainservice.entity.Chapter;
import kz.bitlab.mainservice.entity.Course;
import kz.bitlab.mainservice.entity.Lesson;

import java.time.LocalDateTime;

/**
 * Shared test fixtures, so that tests do not duplicate object construction.
 */
public final class TestData {

    public static final Long COURSE_ID = 1L;
    public static final Long CHAPTER_ID = 3L;
    public static final Long LESSON_ID = 5L;
    public static final Long MISSING_ID = 999L;

    public static final String COURSE_NAME = "Java Developer";
    public static final String COURSE_DESCRIPTION = "A complete path from the first line of Java code to backend development.";
    public static final String CHAPTER_NAME = "if-else conditions";
    public static final String CHAPTER_DESCRIPTION = "Making decisions in code with if, else if and else.";
    public static final String LESSON_NAME = "Lecture for if-else conditions";
    public static final String LESSON_DESCRIPTION = "Theory: conditional statements and logical operators.";
    public static final String LESSON_CONTENT = "<h2>Making decisions with if-else</h2><p>The if statement executes a block only when its condition is true.</p>";

    private static final LocalDateTime TIME = LocalDateTime.of(2026, 10, 13, 10, 15, 30);

    private TestData() {
    }

    public static Course course() {
        return Course.builder()
                .id(COURSE_ID)
                .name(COURSE_NAME)
                .description(COURSE_DESCRIPTION)
                .createdTime(TIME)
                .updatedTime(TIME)
                .build();
    }

    public static CourseDto courseDto() {
        return CourseDto.builder()
                .id(COURSE_ID)
                .name(COURSE_NAME)
                .description(COURSE_DESCRIPTION)
                .createdTime(TIME)
                .updatedTime(TIME)
                .build();
    }

    public static CourseCreateRequest courseCreateRequest() {
        return CourseCreateRequest.builder()
                .name(COURSE_NAME)
                .description(COURSE_DESCRIPTION)
                .build();
    }

    public static CourseUpdateRequest courseUpdateRequest() {
        return CourseUpdateRequest.builder()
                .name("Java Backend Developer")
                .description("Java, Spring Boot, PostgreSQL and microservices.")
                .build();
    }

    public static Chapter chapter() {
        return Chapter.builder()
                .id(CHAPTER_ID)
                .name(CHAPTER_NAME)
                .description(CHAPTER_DESCRIPTION)
                .order(3)
                .course(course())
                .createdTime(TIME)
                .updatedTime(TIME)
                .build();
    }

    public static ChapterDto chapterDto() {
        return ChapterDto.builder()
                .id(CHAPTER_ID)
                .name(CHAPTER_NAME)
                .description(CHAPTER_DESCRIPTION)
                .order(3)
                .courseId(COURSE_ID)
                .createdTime(TIME)
                .updatedTime(TIME)
                .build();
    }

    public static ChapterCreateRequest chapterCreateRequest() {
        return ChapterCreateRequest.builder()
                .name(CHAPTER_NAME)
                .description(CHAPTER_DESCRIPTION)
                .order(3)
                .courseId(COURSE_ID)
                .build();
    }

    public static ChapterUpdateRequest chapterUpdateRequest() {
        return ChapterUpdateRequest.builder()
                .name("Conditional statements: if, else if, else")
                .description("Branching logic, logical operators and switch.")
                .order(3)
                .build();
    }

    public static Lesson lesson() {
        return Lesson.builder()
                .id(LESSON_ID)
                .name(LESSON_NAME)
                .description(LESSON_DESCRIPTION)
                .content(LESSON_CONTENT)
                .order(1)
                .chapter(chapter())
                .createdTime(TIME)
                .updatedTime(TIME)
                .build();
    }

    public static LessonDto lessonDto() {
        return LessonDto.builder()
                .id(LESSON_ID)
                .name(LESSON_NAME)
                .description(LESSON_DESCRIPTION)
                .content(LESSON_CONTENT)
                .order(1)
                .chapterId(CHAPTER_ID)
                .createdTime(TIME)
                .updatedTime(TIME)
                .build();
    }

    public static LessonCreateRequest lessonCreateRequest() {
        return LessonCreateRequest.builder()
                .name(LESSON_NAME)
                .description(LESSON_DESCRIPTION)
                .content(LESSON_CONTENT)
                .order(1)
                .chapterId(CHAPTER_ID)
                .build();
    }

    public static LessonUpdateRequest lessonUpdateRequest() {
        return LessonUpdateRequest.builder()
                .name(LESSON_NAME)
                .description("Theory: conditional statements, ternary operator and switch.")
                .content(LESSON_CONTENT)
                .order(1)
                .build();
    }
}
