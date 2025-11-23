package com.cloudread.DTO.Request.Authenticate;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @Size(min = 8, max = 32, message = "Password length between 8 - 32 characters")
    String password;

    @Size(min = 8, max = 32, message = "Password length between 8 - 32 characters")
    String confirmPassword;
}
