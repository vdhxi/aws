package com.cloudread.Controller;

import com.cloudread.DTO.Request.Book.*;
import com.cloudread.DTO.Response.ApiResponse;
import com.cloudread.DTO.Response.Book.BookResponse;
import com.cloudread.Service.BooksService;
import com.cloudread.Service.Impl.BooksServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/book")
public class BookController {
    BooksService booksService;

    // Endpoint use for admin perform CRUD on books
    @GetMapping()
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAllBooks() {
        var result = booksService.getAllBooks();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<BookResponse>>builder()
                        .data(result)
                        .build()
        );
    }
    
    @GetMapping("/my-favorites")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAllMyFavorite() {
        var result = booksService.getAllMyFavorites();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<BookResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/author/{authorId}/books")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByAuthor(@PathVariable ("authorId") int authorId) {
        var result = booksService.getAllByAuthor(authorId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<BookResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable ("bookId") int bookId) {
        var result = booksService.getBookById(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<BookResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BookResponse>>> searchBooksByKeyword(@RequestParam ("keyword") String keyword) {
        var result = booksService.searchByKeyword(keyword);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<BookResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/newest")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getNewestBooks() {
        var result = booksService.getNewestBooks();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<BookResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/most-favorite")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getMostFavoriteBooks() {
        var result = booksService.getMostFavoriteBooks();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<BookResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/category/{categoryId}/books")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByCategory(@PathVariable ("categoryId") int categoryId) {
        var result = booksService.getAllByCategory(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<BookResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@RequestPart ("data") @Valid BookCreateRequest request,
                                                                      @RequestPart ("image") MultipartFile coverImage,
                                                                      @RequestPart ("file") MultipartFile bookFile) {
        var result = booksService.createBook(request, coverImage, bookFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<BookResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{bookId}/update")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(@PathVariable ("bookId") int bookId,
                                                                      @RequestPart ("data") @Valid BookUpdateRequest request,
                                                                      @RequestPart(value = "image", required = false) MultipartFile coverImage,
                                                                      @RequestPart(value = "file", required = false) MultipartFile bookFile) {
        var result = booksService.updateBook(request, bookId, coverImage, bookFile);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<BookResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{bookId}/change-status")
    public ResponseEntity<ApiResponse<Object>> changeBookStatus(@PathVariable ("bookId") int bookId) {
        booksService.changeBookStatus(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .message("Status changed successfully")
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Object>> deleteBook(@PathVariable ("bookId") int bookId) {
        booksService.deleteBookById(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .message("Deleted successfully")
                        .build()
        );
    }

    @PutMapping("/{bookId}/favorite")
    public ResponseEntity<ApiResponse<Object>> toggleFavoriteBook(@PathVariable ("bookId") int bookId) {
        booksService.markFavorite(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .message("Toggled successfully")
                        .build()
        );
    }

}
