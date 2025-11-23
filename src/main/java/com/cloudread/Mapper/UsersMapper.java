package com.cloudread.Mapper;

import com.cloudread.DTO.Request.User.UserCreateRequest;
import com.cloudread.DTO.Response.User.UserResponse;
import com.cloudread.Entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsersMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "role", ignore = true)
    Users toUser(UserCreateRequest request);

    @Mapping(target = "active", source = "active")
    UserResponse toResponse(Users user);
}
