package com.cloudread.Controller;

import com.cloudread.DTO.Request.Category.CategoryCreateRequest;
import com.cloudread.DTO.Request.Category.CategoryUpdateRequest;
import com.cloudread.DTO.Response.ApiResponse;
import com.cloudread.DTO.Response.Category.CategoryResponse;
import com.cloudread.Service.CategoriesService;
import com.cloudread.Service.Impl.CategoriesServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoriesService categoriesService;

    // Endpoint to get all categories (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        var result = categoriesService.getAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<CategoryResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {
        var result = categoriesService.getAllActiveCategories();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<CategoryResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable int categoryId) {
        var result = categoriesService.getCategoryById(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<CategoryResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@RequestBody @Valid CategoryCreateRequest request) {
        var result = categoriesService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CategoryResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable int categoryId,
                                                                        @RequestBody @Valid CategoryUpdateRequest request) {
        var result = categoriesService.updateCategory(request, categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<CategoryResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{categoryId}/change-status")
    public ResponseEntity<ApiResponse<Object>> changeCategoryStatus(@PathVariable int categoryId) {
        categoriesService.changeStatus(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .message("Category status changed successfully")
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Object>> deleteCategory(@PathVariable int categoryId) {
        categoriesService.deleteCategory(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .message("Category deleted successfully")
                        .build()
        );
    }
}

