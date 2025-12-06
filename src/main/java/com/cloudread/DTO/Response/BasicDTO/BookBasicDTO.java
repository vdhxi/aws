package com.cloudread.DTO.Response.BasicDTO;

import com.cloudread.DTO.Response.Category.CategoryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookBasicDTO {
    int id;
    String title;
    String description;
    String coverUrl;
    String fileUrl;
    List<CategoryResponse> categories;
}
