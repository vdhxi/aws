package com.cloudread.Service.Impl;

import com.cloudread.DTO.Request.Category.CategoryCreateRequest;
import com.cloudread.DTO.Request.Category.CategoryUpdateRequest;
import com.cloudread.DTO.Response.Category.CategoryResponse;
import com.cloudread.Entity.Categories;
import com.cloudread.Entity.Book;
import com.cloudread.Entity.Users;
import com.cloudread.Enum.Role;
import com.cloudread.Exception.AppException;
import com.cloudread.Exception.ErrorCode;
import com.cloudread.Mapper.CategoriesMapper;
import com.cloudread.Repository.CategoriesRepository;
import com.cloudread.Repository.BookRepository;
import com.cloudread.Repository.UserRepository;
import com.cloudread.Service.CategoriesService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoriesServiceImpl implements CategoriesService {
    CategoriesRepository categoriesRepository;
    CategoriesMapper categoriesMapper;
    BookRepository bookRepository;
    UserRepository userRepository;


    public List<CategoryResponse> getAllCategories() {
        Users user = getUserBySecurityContextHolder();
        if (user.getRole() != Role.ROLE_ADMIN) {
            return categoriesRepository.findByActive(true).stream().map(categoriesMapper::toResponse).toList();
        } else {
            return categoriesRepository.findAll().stream().map(categoriesMapper::toResponse).toList();
        }
    }

    public CategoryResponse getCategoryById(int categoryId) {
        Categories category = findById(categoryId);
        if (!category.isActive()) {
            Users userLogin = getUserBySecurityContextHolder();
            if (userLogin.getRole() != Role.ROLE_ADMIN) {
                throw new AppException(ErrorCode.NOT_FOUND);
            }
        }
        return categoriesMapper.toResponse(category);
    }

    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (findByName(request.getName()) != null) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        Categories category = categoriesMapper.toCategory(request);

        category.setActive(true);

        // Save
        categoriesRepository.save(category);
        return categoriesMapper.toResponse(category);
    }

    public CategoryResponse updateCategory(CategoryUpdateRequest request, int id) {
        Categories category = findById(id);

        if (findByName(request.getName()) != null && !category.getName().equalsIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        categoriesMapper.updateCategory(category, request);
        // Save
        categoriesRepository.save(category);
        return categoriesMapper.toResponse(category);
    }

    public void deleteCategory(int id) {
        Categories category = findById(id);
        categoriesRepository.delete(category);
    }

    public void changeStatus(int id) {
        Categories category = findById(id);
        category.setActive(!category.isActive());

        if (!category.isActive()) {
            List<Book> bookList = category.getBooks();
            bookList.forEach(singleBook -> {
                singleBook.setActive(false);
                bookRepository.save(singleBook);
            });
        }

        categoriesRepository.save(category);
    }

    private Categories findById(int id) {
        return categoriesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private Categories findByName(String name) {
        return categoriesRepository.findByName(name).orElse(null);
    }

    private Users getUserBySecurityContextHolder() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        } else  {
            return user;
        }
    }

}
