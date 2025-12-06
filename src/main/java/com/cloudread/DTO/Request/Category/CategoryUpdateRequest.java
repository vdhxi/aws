package com.cloudread.DTO.Request.Category;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryUpdateRequest {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String name;

    Boolean isActive;
}


