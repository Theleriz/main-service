package kz.bitlab.mainservice.controller;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.config.WithSecurityConfig;
import kz.bitlab.mainservice.dto.UserCreateRequest;
import kz.bitlab.mainservice.dto.UserUpdateRequest;
import kz.bitlab.mainservice.exception.ConflictException;
import kz.bitlab.mainservice.security.AuthUser;
import kz.bitlab.mainservice.security.Role;
import kz.bitlab.mainservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static kz.bitlab.mainservice.TestData.USERNAME;
import static kz.bitlab.mainservice.TestData.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@WithSecurityConfig
class UserControllerTest {

    private static final String CREATE_BODY = """
            {"username": "user", "email": "user@bitlab.kz", "firstName": "User", "lastName": "Bitlab",
             "password": "secret123", "roles": ["ROLE_TEACHER"]}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void create_asAdmin_returns201() throws Exception {
        when(userService.create(any(UserCreateRequest.class))).thenReturn(TestData.userDto(Role.ROLE_TEACHER));

        mockMvc.perform(post("/api/v1/users")
                        .with(user(TestData.authUser(Role.ROLE_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_TEACHER"));
    }

    @Test
    void create_asTeacher_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(user(TestData.authUser(Role.ROLE_TEACHER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(userService, never()).create(any());
    }

    @Test
    void create_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).create(any());
    }

    @Test
    void create_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(user(TestData.authUser(Role.ROLE_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "user", "email": "not-an-email", "firstName": "User",
                                 "lastName": "Bitlab", "password": "secret123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void create_existingUsername_returns409() throws Exception {
        when(userService.create(any(UserCreateRequest.class)))
                .thenThrow(new ConflictException("User with the same username or email already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .with(user(TestData.authUser(Role.ROLE_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getCurrent_returnsUserOfToken() throws Exception {
        when(userService.getById(USER_ID)).thenReturn(TestData.userDto(Role.ROLE_USER));

        mockMvc.perform(get("/api/v1/users/me").with(user(TestData.authUser(Role.ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID));
    }

    @Test
    void updateCurrent_asUser_returns200() throws Exception {
        AuthUser currentUser = TestData.authUser(Role.ROLE_USER);
        when(userService.updateCurrent(eq(currentUser), any(UserUpdateRequest.class)))
                .thenReturn(TestData.userDto(Role.ROLE_USER));

        mockMvc.perform(put("/api/v1/users/me")
                        .with(user(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Jane", "password": "newSecret123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME));
    }

    @Test
    void updateCurrent_userChangesOwnRoles_returns403() throws Exception {
        when(userService.updateCurrent(any(AuthUser.class), any(UserUpdateRequest.class)))
                .thenThrow(new AccessDeniedException("Only an admin can change roles"));

        mockMvc.perform(put("/api/v1/users/me")
                        .with(user(TestData.authUser(Role.ROLE_USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roles": ["ROLE_ADMIN"]}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only an admin can change roles"));
    }

    @Test
    void updateCurrent_shortPassword_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                        .with(user(TestData.authUser(Role.ROLE_USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void update_asAdmin_returns200() throws Exception {
        when(userService.update(eq(USER_ID), any(UserUpdateRequest.class)))
                .thenReturn(TestData.userDto(Role.ROLE_TEACHER));

        mockMvc.perform(put("/api/v1/users/{id}", USER_ID)
                        .with(user(TestData.authUser(Role.ROLE_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roles": ["ROLE_TEACHER"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_TEACHER"));
    }

    @Test
    void update_asUser_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/users/{id}", USER_ID)
                        .with(user(TestData.authUser(Role.ROLE_USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roles": ["ROLE_ADMIN"]}
                                """))
                .andExpect(status().isForbidden());

        verify(userService, never()).update(any(), any());
    }
}
