package kz.bitlab.mainservice.service.impl;

import kz.bitlab.mainservice.dto.UserCreateRequest;
import kz.bitlab.mainservice.dto.UserDto;
import kz.bitlab.mainservice.dto.UserUpdateRequest;
import kz.bitlab.mainservice.keycloak.KeycloakClient;
import kz.bitlab.mainservice.keycloak.dto.KeycloakCredential;
import kz.bitlab.mainservice.keycloak.dto.KeycloakRole;
import kz.bitlab.mainservice.keycloak.dto.KeycloakUser;
import kz.bitlab.mainservice.mapper.UserMapper;
import kz.bitlab.mainservice.security.AuthUser;
import kz.bitlab.mainservice.security.Role;
import kz.bitlab.mainservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KeycloakClient keycloakClient;
    private final UserMapper userMapper;

    @Override
    public UserDto getById(String id) {
        log.info("Fetching user with id {}", id);
        return toDto(keycloakClient.getUser(id));
    }

    @Override
    public UserDto create(UserCreateRequest request) {
        log.info("Creating user {}", request.getUsername());
        log.debug("User create request: {}", request);
        KeycloakUser user = userMapper.toKeycloakUser(request);
        user.setCredentials(List.of(KeycloakCredential.password(request.getPassword())));
        String id = keycloakClient.createUser(user);

        Set<Role> roles = request.getRoles() == null || request.getRoles().isEmpty()
                ? EnumSet.of(Role.ROLE_USER)
                : request.getRoles();
        keycloakClient.addUserRealmRoles(id, toKeycloakRoles(roles));
        log.info("User {} created with id {} and roles {}", request.getUsername(), id, roles);
        return getById(id);
    }

    @Override
    public UserDto updateCurrent(AuthUser currentUser, UserUpdateRequest request) {
        if (request.getRoles() != null && !currentUser.isAdmin()) {
            log.warn("User {} tried to change own roles without ROLE_ADMIN", currentUser.getUsername());
            throw new AccessDeniedException("Only an admin can change roles");
        }
        return update(currentUser.getId(), request);
    }

    @Override
    public UserDto update(String id, UserUpdateRequest request) {
        log.info("Updating user with id {}", id);
        log.debug("User update request: {}", request);
        KeycloakUser user = keycloakClient.getUser(id);
        userMapper.updateKeycloakUser(request, user);
        keycloakClient.updateUser(id, user);

        if (request.getPassword() != null) {
            keycloakClient.resetPassword(id, request.getPassword());
            log.info("Password of user with id {} changed", id);
        }
        if (request.getRoles() != null) {
            replaceRoles(id, request.getRoles());
        }
        return getById(id);
    }

    /**
     * Makes the user's application roles exactly equal to the given set.
     * Keycloak's own roles (default-roles-lms, offline_access ...) are not touched.
     */
    private void replaceRoles(String id, Set<Role> roles) {
        Set<String> wanted = roles.stream().map(Role::name).collect(Collectors.toSet());
        List<KeycloakRole> current = keycloakClient.getUserRealmRoles(id).stream()
                .filter(role -> Role.isKnown(role.getName()))
                .toList();
        Set<String> currentNames = current.stream().map(KeycloakRole::getName).collect(Collectors.toSet());

        List<KeycloakRole> toRemove = current.stream()
                .filter(role -> !wanted.contains(role.getName()))
                .toList();
        List<KeycloakRole> toAdd = toKeycloakRoles(roles.stream()
                .filter(role -> !currentNames.contains(role.name()))
                .collect(Collectors.toSet()));

        keycloakClient.removeUserRealmRoles(id, toRemove);
        keycloakClient.addUserRealmRoles(id, toAdd);
        log.info("Roles of user with id {} set to {}", id, wanted);
    }

    private List<KeycloakRole> toKeycloakRoles(Set<Role> roles) {
        return roles.stream()
                .map(role -> keycloakClient.getRealmRole(role.name()))
                .toList();
    }

    private UserDto toDto(KeycloakUser user) {
        Set<Role> roles = keycloakClient.getUserRealmRoles(user.getId()).stream()
                .map(KeycloakRole::getName)
                .filter(Role::isKnown)
                .map(Role::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
        return userMapper.toDto(user, roles);
    }
}
