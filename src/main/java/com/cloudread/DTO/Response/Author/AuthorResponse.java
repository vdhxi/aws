package com.cloudread.DTO.Response.Author;

import com.cloudread.Entity.Book;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorResponse {
    int id;
    String name;
    String imageUrl;
    String description;
    boolean isActive;
}


