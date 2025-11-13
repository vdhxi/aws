package com.cloudread.Service;

import com.cloudread.DTO.Request.Author.AuthorCreateRequest;
import com.cloudread.DTO.Request.Author.AuthorUpdateRequest;
import com.cloudread.DTO.Response.Author.AuthorResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuthorsService {
    AuthorResponse getAuthorById(int id);

    List<AuthorResponse> getAllAuthors();

    AuthorResponse createAuthor(AuthorCreateRequest request, MultipartFile image);

    AuthorResponse updateAuthor(AuthorUpdateRequest request, int id, MultipartFile image);

    void changeStatus(int id);

    void deleteAuthorById(int id);
}