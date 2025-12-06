package com.cloudread.DTO.Request.Mail;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SendMailRequest {
    String sendTo;
    String subject;
    String message;
}