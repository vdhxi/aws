package com.cloudread.DTO.Request.Authenticate;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @Size(min = 4, message = "At least 4 characters")
    String input;

    @Size(min = 8, max = 32, message = "Password length between 8 - 32 characters")
    String password;
}
