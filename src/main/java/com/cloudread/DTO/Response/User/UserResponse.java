package com.cloudread.DTO.Response.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;

    private String username;

    private String email;

    private boolean active;

}
