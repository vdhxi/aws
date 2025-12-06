package com.cloudread.Service;

import com.cloudread.DTO.Request.Authenticate.*;
import com.cloudread.DTO.Response.Authenticate.IntrospectResponse;
import com.cloudread.DTO.Response.Authenticate.LoginResponse;
import com.cloudread.Enum.TokenScope;
import com.cloudread.Enum.TokenStep;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;

public interface AuthenticateService {
    LoginResponse login(LoginRequest request);

    SignedJWT verifyToken(String token, boolean refresh) throws JOSEException, ParseException;

    IntrospectResponse introspect(IntrospectRequest request);

    String verifyEmailExist(String email, TokenScope scope);

    String generateAccessTokenByScopeAndStep(String subjectToken, TokenScope tokenScope, TokenStep tokenStep);

    String generateRequestTokenByScope(TokenScope tokenScope);

    String generateRegisterToken(String email);

    String generateChangeEmailToken(String subject, String email);

    String generateResetPasswordToken(String subjectToken, TokenScope tokenScope, TokenStep tokenStep);

    String resetPasswordRequest(ResetPasswordRequest request);

    String verifyCurrentPassword(VerifyCurrentPasswordRequest request, TokenScope tokenScope);

    String verifyEmailOtpCode(String token, TokenScope tokenScope, String otp);

    String verifyForgetPasswordOtp(String token, String otp);

    void logout(LogoutRequest request) throws ParseException, JOSEException;

    LoginResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;

    void checkTokenIsAllowToUse(String token);

    void deleteTokenAfterUse(String token);

    boolean validateTokenByScopeAndStep(String token, TokenScope scopeToCheck, TokenStep stepToCheck) throws JOSEException, ParseException;
}