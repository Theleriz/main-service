package kz.bitlab.mainservice;

import kz.bitlab.mainservice.dto.AttachmentDto;
import kz.bitlab.mainservice.dto.ChapterCreateRequest;
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.ChapterUpdateRequest;
import kz.bitlab.mainservice.dto.CourseCreateRequest;
import kz.bitlab.mainservice.dto.CourseDto;
import kz.bitlab.mainservice.dto.CourseUpdateRequest;
import kz.bitlab.mainservice.dto.LessonCreateRequest;
import kz.bitlab.mainservice.dto.LessonDto;
import kz.bitlab.mainservice.dto.LessonUpdateRequest;
import kz.bitlab.mainservice.dto.TokenResponse;
import kz.bitlab.mainservice.dto.UserCreateRequest;
import kz.bitlab.mainservice.dto.UserDto;
import kz.bitlab.mainservice.entity.Attachment;
import kz.bitlab.mainservice.entity.Chapter;
import kz.bitlab.mainservice.entity.Course;
import kz.bitlab.mainservice.entity.Lesson;
import kz.bitlab.mainservice.keycloak.dto.KeycloakTokenResponse;
import kz.bitlab.mainservice.keycloak.dto.KeycloakUser;
import kz.bitlab.mainservice.security.AuthUser;
import kz.bitlab.mainservice.security.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

/**
 * Shared test fixtures, so that tests do not duplicate object construction.
 */
public final class TestData {

    public static final Long COURSE_ID = 1L;
    public static final Long CHAPTER_ID = 3L;
    public static final Long LESSON_ID = 5L;
    public static final Long MISSING_ID = 999L;
    public static final Long ATTACHMENT_ID = 7L;
    public static final String ATTACHMENT_NAME = "if-else-cheatsheet.pdf";
    public static final String ATTACHMENT_URL = "lessons/5/3f2b8c1e-6d4a-4f1b-9a7e-2c5d8e9f0a1b.pdf";

    public static final String COURSE_NAME = "Java Developer";
    public static final String COURSE_DESCRIPTION = "A complete path from the first line of Java code to backend development.";
    public static final String CHAPTER_NAME = "if-else conditions";
    public static final String CHAPTER_DESCRIPTION = "Making decisions in code with if, else if and else.";
    public static final String LESSON_NAME = "Lecture for if-else conditions";
    public static final String LESSON_DESCRIPTION = "Theory: conditional statements and logical operators.";
    public static final String LESSON_CONTENT = "<h2>Making decisions with if-else</h2><p>The if statement executes a block only when its condition is true.</p>";

    public static final String USER_ID = "5c2e8b1a-4f3d-4e7a-8c6b-0b0000000003";
    public static final String USERNAME = "user";
    public static final String USER_EMAIL = "user@bitlab.kz";
    public static final String ACCESS_TOKEN = "access-token";
    public static final String REFRESH_TOKEN = "refresh-token";

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

    public static AuthUser authUser(Role... roles) {
        return new AuthUser(USER_ID, USERNAME, USER_EMAIL, Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList());
    }

    public static KeycloakUser keycloakUser() {
        return KeycloakUser.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(USER_EMAIL)
                .firstName("User")
                .lastName("Bitlab")
                .enabled(true)
                .emailVerified(true)
                .build();
    }

    public static UserDto userDto(Role... roles) {
        return UserDto.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(USER_EMAIL)
                .firstName("User")
                .lastName("Bitlab")
                .enabled(true)
                .roles(Set.of(roles))
                .build();
    }

    public static UserCreateRequest userCreateRequest() {
        return UserCreateRequest.builder()
                .username(USERNAME)
                .email(USER_EMAIL)
                .firstName("User")
                .lastName("Bitlab")
                .password("secret123")
                .build();
    }

    public static KeycloakTokenResponse keycloakTokenResponse() {
        return KeycloakTokenResponse.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .tokenType("Bearer")
                .expiresIn(300L)
                .refreshExpiresIn(604800L)
                .build();
    }

    public static TokenResponse tokenResponse() {
        return TokenResponse.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .tokenType("Bearer")
                .expiresIn(300L)
                .refreshExpiresIn(604800L)
                .build();
    }

    public static Attachment attachment() {
        return Attachment.builder()
                .id(ATTACHMENT_ID)
                .name(ATTACHMENT_NAME)
                .url(ATTACHMENT_URL)
                .lesson(lesson())
                .createdTime(TIME)
                .build();
    }

    public static AttachmentDto attachmentDto() {
        return AttachmentDto.builder()
                .id(ATTACHMENT_ID)
                .name(ATTACHMENT_NAME)
                .url(ATTACHMENT_URL)
                .lessonId(LESSON_ID)
                .createdTime(TIME)
                .build();
    }
}
