package com.cloudread.Controller;

import com.cloudread.DTO.Request.Author.AuthorCreateRequest;
import com.cloudread.DTO.Request.Author.AuthorUpdateRequest;
import com.cloudread.DTO.Response.ApiResponse;
import com.cloudread.DTO.Response.Author.AuthorResponse;
import com.cloudread.Service.AuthorsService;
import com.cloudread.Service.Impl.AuthorsServiceImpl;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/author")
public class AuthorController {
    AuthorsService authorsService;

    // Endpoint to get all authors (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AuthorResponse>>> getAllAuthors() {
        var result = authorsService.getAllAuthors();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<AuthorResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorResponse>>> getActiveAuthors() {
        var result = authorsService.getAllActiveAuthors();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<AuthorResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/{authorId}")
    public ResponseEntity<ApiResponse<AuthorResponse>> getAuthorById(@PathVariable int authorId) {
        var result = authorsService.getAuthorById(authorId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<AuthorResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<AuthorResponse>> createAuthor(@RequestPart (value = "data")  @Valid AuthorCreateRequest request,
                                                                    @RequestPart (value = "image") MultipartFile image) {
        var result = authorsService.createAuthor(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<AuthorResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{authorId}")
    public ResponseEntity<ApiResponse<AuthorResponse>> updateAuthor(@PathVariable int authorId,
                                                                    @RequestPart (value = "data") @Valid AuthorUpdateRequest request,
                                                                    @RequestPart (value = "image", required = false) MultipartFile image) {
        var result = authorsService.updateAuthor(request, authorId, image);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<AuthorResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{authorId}/change-status")
    public ResponseEntity<ApiResponse<String>> changeStatus(@PathVariable int authorId) {
        authorsService.changeStatus(authorId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<String>builder()
                        .data("Author status changed successfully")
                        .build()
        );
    }
}
