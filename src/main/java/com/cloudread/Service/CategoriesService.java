package com.cloudread.Service;

import com.cloudread.DTO.Request.Category.CategoryCreateRequest;
import com.cloudread.DTO.Request.Category.CategoryUpdateRequest;
import com.cloudread.DTO.Response.Category.CategoryResponse;

import java.util.List;

public interface CategoriesService {
    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(int categoryId);

    CategoryResponse createCategory(CategoryCreateRequest request);

    CategoryResponse updateCategory(CategoryUpdateRequest request, int id);

    void deleteCategory(int id);

    void changeStatus(int id);
}