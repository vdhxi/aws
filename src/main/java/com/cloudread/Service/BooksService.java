package com.cloudread.Service;

import com.cloudread.DTO.Request.Book.BookCreateRequest;
import com.cloudread.DTO.Request.Book.BookUpdateRequest;
import com.cloudread.DTO.Response.Book.BookResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BooksService {
    BookResponse getBookById(int id);

    List<BookResponse> getAllBooks();

    List<BookResponse> getAllActiveBook();

    List<BookResponse> getAllMyFavorites();

    List<BookResponse> getAllByAuthor(int authorId);

    List<BookResponse> searchByKeyword(String keyword);

    List<BookResponse> getAllByCategory(int categoryId);

    List<BookResponse> getNewestBooks();

    List<BookResponse> getMostFavoriteBooks();

    BookResponse createBook(BookCreateRequest request, MultipartFile coverImage, MultipartFile file);

    BookResponse updateBook(BookUpdateRequest request, int id, MultipartFile coverImage, MultipartFile file);

    void deleteBookById(int id);

    void changeBookStatus(int id);

    void markFavorite(int id);
}