package com.cloudread.Mapper;

import com.cloudread.DTO.Request.Author.AuthorCreateRequest;
import com.cloudread.DTO.Request.Author.AuthorUpdateRequest;
import com.cloudread.DTO.Response.Author.AuthorResponse;
import com.cloudread.Entity.Authors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuthorsMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    Authors toAuthor(AuthorCreateRequest request);

    @Mapping(target = "isActive", source = "active")
    AuthorResponse toResponse(Authors author);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "active", source = "isActive")
    void updateAuthor(@MappingTarget Authors author, AuthorUpdateRequest request);
}
