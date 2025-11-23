package com.cloudread.DTO.Response.Book;

import com.cloudread.DTO.Response.BasicDTO.AuthorBasicDTO;
import com.cloudread.DTO.Response.Category.CategoryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    int id;
    String title;
    String description;
    String coverUrl;
    String fileUrl;
    int favorite;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    AuthorBasicDTO author;
    List<CategoryResponse> categories;
}


