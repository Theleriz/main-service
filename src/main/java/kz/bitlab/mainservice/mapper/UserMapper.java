package kz.bitlab.mainservice.mapper;

import kz.bitlab.mainservice.dto.UserCreateRequest;
import kz.bitlab.mainservice.dto.UserDto;
import kz.bitlab.mainservice.dto.UserUpdateRequest;
import kz.bitlab.mainservice.keycloak.dto.KeycloakUser;
import kz.bitlab.mainservice.security.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;

@Mapper
public interface UserMapper {

    UserDto toDto(KeycloakUser user, Set<Role> roles);

    // Password goes to credentials and roles to role mappings, both are set by the service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "true")
    @Mapping(target = "credentials", ignore = true)
    KeycloakUser toKeycloakUser(UserCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "credentials", ignore = true)
    void updateKeycloakUser(UserUpdateRequest request, @MappingTarget KeycloakUser user);
}
