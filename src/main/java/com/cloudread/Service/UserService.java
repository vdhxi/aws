package com.cloudread.Service;

import com.cloudread.DTO.Request.Authenticate.ChangePasswordRequest;
import com.cloudread.DTO.Request.User.UserCreateRequest;
import com.cloudread.DTO.Request.User.UserUpdateRequest;
import com.cloudread.DTO.Response.User.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();

    UserResponse getUserById(String id);

    UserResponse createUser(UserCreateRequest request, String token);

    UserResponse updateUser(UserUpdateRequest request);

    UserResponse changeEmail(String token);

    void changePassword(String token, ChangePasswordRequest request);
}