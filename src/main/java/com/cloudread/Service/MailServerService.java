package com.cloudread.Service;

import com.cloudread.DTO.Request.Mail.SendMailRequest;

public interface MailServerService {
    void sendMail(String to, String subject, String text);

    void sendMail(SendMailRequest request);
}


