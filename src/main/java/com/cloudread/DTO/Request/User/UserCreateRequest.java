package com.cloudread.DTO.Request.User;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
public class UserCreateRequest {
    @Size(min = 4, max = 20, message = "Username between 4 - 20 characters")
    String username;

    @Size(min = 8, max = 32, message = "Password length between 8 - 32 characters")
    String password;

    @Size(min = 8, max = 32, message = "Password length between 8 - 32 characters")
    String confirmPassword;
}
