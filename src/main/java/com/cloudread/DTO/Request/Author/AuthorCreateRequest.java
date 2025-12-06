package com.cloudread.DTO.Request.Author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorCreateRequest {
    @NotBlank(message = "Author name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description;
}


