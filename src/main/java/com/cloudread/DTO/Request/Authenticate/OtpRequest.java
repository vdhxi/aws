package com.cloudread.DTO.Request.Authenticate;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class OtpRequest {
    @Size(min = 6, max = 6)
    String otp;
}