package com.cloudread.Utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailMessage {
    public static String CREATE_ACCOUNT_SUBJECT = "Welcome to Read on cloud!";

    public static String CREATE_ACCOUNT_MESSAGE = "Hello, thank you for joining Read on cloud. We’re excited to have you on board!";

    public static String CHANGE_PASSWORD_SUCCESS_SUBJECT = "Your Password Has Been Successfully Changed";

    public static String CHANGE_PASSWORD_SUCCESS_MESSAGE = "If you didn’t request this change, please contact our support team immediately.";

    public static String REQUEST_RESET_PASSWORD_SUBJECT = "Your Password Reset Request";

    public static String REQUEST_RESET_PASSWORD_MESSAGE = """
            Hello, we have received a request to reset your password. \
            If this was you, please click the link below to reset it:
            
            %s
            
            If you didn’t request this, please contact our support team immediately.""";

    public static String VERIFY_EMAIL_UPDATE_SUBJECT = "Read on cloud — Confirm your account update";

    public static String VERIFY_EMAIL_UPDATE_MESSAGE = """
    Hello,

    We received a request to update your account information on Read on cloud. To confirm this action, please enter the following One-Time Password (OTP):

    Your OTP code: %s

    This code is valid for 10 minutes and can only be used once.

    If you did not request this change, please secure your account immediately or contact our support team.

    — Read on cloud Team
    """;

}
