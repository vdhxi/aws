package com.cloudread.Mapper;

import com.cloudread.DTO.Request.Category.CategoryCreateRequest;
import com.cloudread.DTO.Request.Category.CategoryUpdateRequest;
import com.cloudread.DTO.Response.Category.CategoryResponse;
import com.cloudread.Entity.Categories;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoriesMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "books", ignore = true)
    Categories toCategory(CategoryCreateRequest request);

    CategoryResponse toResponse(Categories category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "active", source = "isActive")
    void updateCategory(@MappingTarget Categories category, CategoryUpdateRequest request);
}
