package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.dto.UserCreateRequest;
import kz.bitlab.mainservice.dto.UserDto;
import kz.bitlab.mainservice.dto.UserUpdateRequest;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.keycloak.KeycloakClient;
import kz.bitlab.mainservice.keycloak.dto.KeycloakRole;
import kz.bitlab.mainservice.keycloak.dto.KeycloakUser;
import kz.bitlab.mainservice.mapper.UserMapper;
import kz.bitlab.mainservice.security.Role;
import kz.bitlab.mainservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Set;

import static kz.bitlab.mainservice.TestData.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final KeycloakRole ADMIN = new KeycloakRole("role-admin", "ROLE_ADMIN");
    private static final KeycloakRole TEACHER = new KeycloakRole("role-teacher", "ROLE_TEACHER");
    private static final KeycloakRole USER = new KeycloakRole("role-user", "ROLE_USER");
    private static final KeycloakRole DEFAULT_ROLES = new KeycloakRole("role-default", "default-roles-lms");

    @Mock
    private KeycloakClient keycloakClient;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void create_withoutRoles_createsUserWithPasswordAndRoleUser() {
        UserCreateRequest request = TestData.userCreateRequest();
        KeycloakUser keycloakUser = TestData.keycloakUser();
        keycloakUser.setId(null);
        when(userMapper.toKeycloakUser(request)).thenReturn(keycloakUser);
        when(keycloakClient.createUser(keycloakUser)).thenReturn(USER_ID);
        when(keycloakClient.getRealmRole("ROLE_USER")).thenReturn(USER);
        stubGetById(List.of(USER, DEFAULT_ROLES), Set.of(Role.ROLE_USER));

        UserDto result = userService.create(request);

        assertThat(result.getRoles()).containsExactly(Role.ROLE_USER);
        assertThat(keycloakUser.getCredentials()).singleElement()
                .satisfies(credential -> {
                    assertThat(credential.getType()).isEqualTo("password");
                    assertThat(credential.getValue()).isEqualTo("secret123");
                    assertThat(credential.getTemporary()).isFalse();
                });
        verify(keycloakClient).addUserRealmRoles(USER_ID, List.of(USER));
    }

    @Test
    void create_withRoles_assignsGivenRoles() {
        UserCreateRequest request = TestData.userCreateRequest();
        request.setRoles(Set.of(Role.ROLE_TEACHER));
        when(userMapper.toKeycloakUser(request)).thenReturn(TestData.keycloakUser());
        when(keycloakClient.createUser(any())).thenReturn(USER_ID);
        when(keycloakClient.getRealmRole("ROLE_TEACHER")).thenReturn(TEACHER);
        stubGetById(List.of(TEACHER), Set.of(Role.ROLE_TEACHER));

        userService.create(request);

        verify(keycloakClient).addUserRealmRoles(USER_ID, List.of(TEACHER));
    }

    @Test
    void updateCurrent_nonAdminSendsRoles_throwsAccessDenied() {
        UserUpdateRequest request = UserUpdateRequest.builder().roles(Set.of(Role.ROLE_ADMIN)).build();

        assertThrows(AccessDeniedException.class,
                () -> userService.updateCurrent(TestData.authUser(Role.ROLE_USER), request));

        verify(keycloakClient, never()).updateUser(anyString(), any());
        verify(keycloakClient, never()).addUserRealmRoles(anyString(), any());
    }

    @Test
    void updateCurrent_nonAdminWithoutRoles_updatesDataAndPassword() {
        UserUpdateRequest request = UserUpdateRequest.builder().firstName("Jane").password("newSecret123").build();
        KeycloakUser keycloakUser = TestData.keycloakUser();
        when(keycloakClient.getUser(USER_ID)).thenReturn(keycloakUser);
        when(keycloakClient.getUserRealmRoles(USER_ID)).thenReturn(List.of(USER));
        when(userMapper.toDto(any(), any())).thenReturn(TestData.userDto(Role.ROLE_USER));

        userService.updateCurrent(TestData.authUser(Role.ROLE_USER), request);

        verify(userMapper).updateKeycloakUser(request, keycloakUser);
        verify(keycloakClient).updateUser(USER_ID, keycloakUser);
        verify(keycloakClient).resetPassword(USER_ID, "newSecret123");
        verify(keycloakClient, never()).addUserRealmRoles(anyString(), any());
        verify(keycloakClient, never()).removeUserRealmRoles(anyString(), any());
    }

    @Test
    void updateCurrent_adminChangesOwnRoles_isAllowed() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_TEACHER)).build();
        when(keycloakClient.getUser(USER_ID)).thenReturn(TestData.keycloakUser());
        when(keycloakClient.getUserRealmRoles(USER_ID)).thenReturn(List.of(ADMIN, DEFAULT_ROLES));
        when(keycloakClient.getRealmRole("ROLE_TEACHER")).thenReturn(TEACHER);
        when(userMapper.toDto(any(), any())).thenReturn(TestData.userDto(Role.ROLE_ADMIN, Role.ROLE_TEACHER));

        userService.updateCurrent(TestData.authUser(Role.ROLE_ADMIN), request);

        verify(keycloakClient).addUserRealmRoles(USER_ID, List.of(TEACHER));
        verify(keycloakClient).removeUserRealmRoles(USER_ID, List.of());
    }

    @Test
    void update_replacesOnlyApplicationRoles() {
        UserUpdateRequest request = UserUpdateRequest.builder().roles(Set.of(Role.ROLE_TEACHER)).build();
        when(keycloakClient.getUser(USER_ID)).thenReturn(TestData.keycloakUser());
        when(keycloakClient.getUserRealmRoles(USER_ID)).thenReturn(List.of(USER, DEFAULT_ROLES));
        when(keycloakClient.getRealmRole("ROLE_TEACHER")).thenReturn(TEACHER);
        when(userMapper.toDto(any(), any())).thenReturn(TestData.userDto(Role.ROLE_TEACHER));

        userService.update(USER_ID, request);

        // default-roles-lms stays, ROLE_USER is replaced with ROLE_TEACHER
        verify(keycloakClient).removeUserRealmRoles(USER_ID, List.of(USER));
        verify(keycloakClient).addUserRealmRoles(USER_ID, List.of(TEACHER));
        verify(keycloakClient, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void update_missingUser_throwsNotFound() {
        when(keycloakClient.getUser("missing")).thenThrow(new NotFoundException("User with id missing not found"));

        assertThrows(NotFoundException.class, () -> userService.update("missing", new UserUpdateRequest()));

        verify(keycloakClient, never()).updateUser(anyString(), any());
    }

    @Test
    void getById_returnsOnlyApplicationRoles() {
        stubGetById(List.of(USER, DEFAULT_ROLES), Set.of(Role.ROLE_USER));

        UserDto result = userService.getById(USER_ID);

        assertThat(result.getId()).isEqualTo(USER_ID);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<Role>> roles = ArgumentCaptor.forClass(Set.class);
        verify(userMapper).toDto(any(), roles.capture());
        assertThat(roles.getValue()).containsExactly(Role.ROLE_USER);
    }

    private void stubGetById(List<KeycloakRole> keycloakRoles, Set<Role> roles) {
        KeycloakUser stored = TestData.keycloakUser();
        when(keycloakClient.getUser(USER_ID)).thenReturn(stored);
        when(keycloakClient.getUserRealmRoles(USER_ID)).thenReturn(keycloakRoles);
        when(userMapper.toDto(any(), any())).thenReturn(TestData.userDto(roles.toArray(Role[]::new)));
    }
}
