package com.cloudread.DTO.Request.Book;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookUpdateRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description;

    Integer authorId;

    List<Integer> categoryIds;
}


