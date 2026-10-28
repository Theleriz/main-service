package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.dto.UserCreateRequest;
import kz.bitlab.mainservice.dto.UserDto;
import kz.bitlab.mainservice.dto.UserUpdateRequest;
import kz.bitlab.mainservice.security.AuthUser;

public interface UserService {

    UserDto getById(String id);

    UserDto create(UserCreateRequest request);

    /**
     * Updates the data of the current user. Roles can be changed only if the current user is an admin.
     */
    UserDto updateCurrent(AuthUser currentUser, UserUpdateRequest request);

    UserDto update(String id, UserUpdateRequest request);
}
