package com.cloudread.Service.Impl;

import com.cloudread.DTO.Request.Authenticate.*;
import com.cloudread.DTO.Response.Authenticate.IntrospectResponse;
import com.cloudread.DTO.Response.Authenticate.LoginResponse;
import com.cloudread.Entity.OtpCode;
import com.cloudread.Entity.TokenExpired;
import com.cloudread.Entity.TokenValid;
import com.cloudread.Entity.Users;
import com.cloudread.Enum.TokenScope;
import com.cloudread.Enum.TokenStep;
import com.cloudread.Exception.AppException;
import com.cloudread.Exception.ErrorCode;
import com.cloudread.Repository.OtpCodeRepository;
import com.cloudread.Repository.TokenExpiredRepository;
import com.cloudread.Repository.TokenValidRepository;
import com.cloudread.Repository.UserRepository;
import com.cloudread.Service.MailServerService;
import com.cloudread.Service.TokenValidationService;
import com.cloudread.Utils.MailMessage;
import com.cloudread.Utils.RandomCode;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.*;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticateService implements com.cloudread.Service.AuthenticateService {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    OtpCodeRepository otpCodeRepository;
    TokenValidRepository tokenValidRepository;
    TokenValidationService tokenValidationService;
    MailServerService mailServerService;
    TokenExpiredRepository tokenExpiredRepository;

    static String EMAIL_REGEX = "^.*@.*$";

    @Value("${jwt.issuer}")
    @NonFinal
    String ISSUER_TOKEN;

    @Value("${jwt.valid-duration}")
    @NonFinal
    int TIMEOUT_TOKEN;

    @Value("${jwt.next-step-valid-duration}")
    @NonFinal
    int TIMEOUT_NEXT_STEP_TOKEN;

    @Value("${jwt.key}")
    @NonFinal
    String JWT_KEY;

    @Value("${jwt.refresh-duration}")
    @NonFinal
    int TIMEOUT_REFRESH_TOKEN;

    public LoginResponse login(LoginRequest request) {
        String input = request.getInput();
        String password = request.getPassword();
        Users userLogin;

        // Find user
        if (input.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD);
        } else if (input.matches(EMAIL_REGEX)) {
            userLogin = userRepository.findByEmail(input).orElse(null);
        } else {
            userLogin = userRepository.findByUsername(input).orElse(null);
        }

        // Validate login credential
        if (userLogin == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        } else if (!userLogin.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        } else if (!passwordEncoder.matches(password, userLogin.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Generate login credit

        String token = generateToken(userLogin);
        // Response
        return LoginResponse.builder()
                .token(token)
                .build();
    }

    public SignedJWT verifyToken(String token, boolean refresh) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(JWT_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date tokenExpirationTime;

        if (refresh) {
            Instant issueInstant = signedJWT.getJWTClaimsSet().getIssueTime().toInstant();
            Instant refreshExpirationInstant = issueInstant.plus(TIMEOUT_REFRESH_TOKEN, ChronoUnit.SECONDS);
            tokenExpirationTime = new Date(refreshExpirationInstant.toEpochMilli());
        } else {
            tokenExpirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        }

        Date currentTime = new Date();

        boolean isSignatureValid = signedJWT.verify(verifier);

        boolean isTokenValid = isSignatureValid && currentTime.before(tokenExpirationTime);

        if (!(isSignatureValid && isTokenValid)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (tokenExpiredRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        String token = request.getToken();
        try {
            verifyToken(token, false);
        } catch (Exception e) {
            // Handle exception (token is invalid or malformed)
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
        return IntrospectResponse.builder()
                .valid(true)
                .build();
    }

    @Transactional
    public String verifyEmailExist(String email, TokenScope scope) {
        if (getUserByEmail(email) != null) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        } else {
            // Generate Otp code
            String otp = generateOtpCodeForEmail(email, scope);

            switch (scope) {
                case REGISTER ->  {
                    return generateRegisterToken(email);
                }
                case CHANGE_EMAIL -> {
                    Users users = getUserBySecurityContextHolder();
                    String userId = users.getId();
                    String emailMessage = String.format(MailMessage.VERIFY_EMAIL_UPDATE_MESSAGE, otp);
                    mailServerService.sendMail(email, MailMessage.VERIFY_EMAIL_UPDATE_SUBJECT, emailMessage);
                    return generateAccessTokenByScopeAndStep(userId, TokenScope.CHANGE_EMAIL, TokenStep.ALTERNATIVE_EMAIL_CONFIRMED);
                }
                default -> throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
    }

    /** Generate token */

    private String generateToken(Users user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        String subjectId = user.getId();

        Map<String,Object> claimsSet = buildClaimsSet(user);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(subjectId)
                .claim("user", claimsSet)
                .claim("tokenScope", TokenScope.LOGIN)
                .claim("scope",user.getRole())
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(TIMEOUT_TOKEN, ChronoUnit.SECONDS).toEpochMilli()))
                .issuer(ISSUER_TOKEN)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(JWT_KEY.getBytes()));
            return jwsObject.serialize();
        }  catch (JOSEException e) {
            throw new RuntimeException();
        }
    }

    @Transactional
    public String generateAccessTokenByScopeAndStep(String subjectToken, TokenScope tokenScope, TokenStep tokenStep) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        int timeToLive = tokenStep.getTimeToLive();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(subjectToken)
                .claim("tokenScope", tokenScope)
                .claim("step", tokenStep)
                .issuer(ISSUER_TOKEN)
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(timeToLive, ChronoUnit.SECONDS).toEpochMilli()))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(JWT_KEY.getBytes()));
            String token = jwsObject.serialize();

            // Save valid token
            TokenValid tokenValid = TokenValid.builder()
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusSeconds(timeToLive))
                    .currentUse(0)
                    .allowToUse(tokenStep.getAllowToUse())
                    .build();

            tokenValidRepository.save(tokenValid);

            return token;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public String generateRequestTokenByScope(TokenScope tokenScope) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        TokenStep tokenStep = TokenStep.PASSWORD_VERIFICATION;

        String userId = getUserBySecurityContextHolder().getId();

        int timeToLive = tokenStep.getTimeToLive();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .claim("tokenScope", tokenScope)
                .claim("step", tokenStep)
                .issuer(ISSUER_TOKEN)
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(timeToLive, ChronoUnit.SECONDS).toEpochMilli()))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(JWT_KEY.getBytes()));
            String token = jwsObject.serialize();

            // Save valid token
            TokenValid tokenValid = TokenValid.builder()
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusSeconds(timeToLive))
                    .currentUse(0)
                    .allowToUse(tokenStep.getAllowToUse())
                    .build();

            tokenValidRepository.save(tokenValid);

            return token;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateRegisterToken(String email) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        TokenStep tokenStep = TokenStep.REGISTER_STEP;
        int timeToLive = tokenStep.getTimeToLive();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .claim("tokenScope", TokenScope.REGISTER)
                .claim("step", tokenStep)
                .issuer(ISSUER_TOKEN)
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(timeToLive, ChronoUnit.SECONDS).toEpochMilli()))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(JWT_KEY.getBytes()));
            String token = jwsObject.serialize();

            TokenValid tokenValid = TokenValid.builder()
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusSeconds(timeToLive))
                    .currentUse(0)
                    .allowToUse(tokenStep.getAllowToUse())
                    .build();

            tokenValidRepository.save(tokenValid);

            return token;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateChangeEmailToken(String subject, String email) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        TokenStep tokenStep = TokenStep.ALTERNATIVE_EMAIL_CONFIRMED;
        int timeToLive = tokenStep.getTimeToLive();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .claim("email", email)
                .claim("tokenScope", TokenScope.CHANGE_EMAIL)
                .claim("step", tokenStep)
                .issuer(ISSUER_TOKEN)
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(timeToLive, ChronoUnit.SECONDS).toEpochMilli()))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);


        try {
            jwsObject.sign(new MACSigner(JWT_KEY.getBytes()));
            String token = jwsObject.serialize();

            TokenValid tokenValid = TokenValid.builder()
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusSeconds(timeToLive))
                    .currentUse(0)
                    .allowToUse(tokenStep.getAllowToUse())
                    .build();
            tokenValidRepository.save(tokenValid);

            return  token;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public String generateResetPasswordToken(String subjectToken, TokenScope tokenScope, TokenStep tokenStep) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        int timeToLive = tokenStep.getTimeToLive();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(subjectToken)
                .claim("tokenScope", tokenScope)
                .claim("step", tokenStep)
                .claim("exist", true)
                .issuer(ISSUER_TOKEN)
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(timeToLive, ChronoUnit.SECONDS).toEpochMilli()))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(JWT_KEY.getBytes()));
            String token = jwsObject.serialize();

            // Save valid token
            TokenValid tokenValid = TokenValid.builder()
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusSeconds(timeToLive))
                    .currentUse(0)
                    .allowToUse(tokenStep.getAllowToUse())
                    .build();

            tokenValidRepository.save(tokenValid);

            return token;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> buildClaimsSet(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        return claims;
    }

    @Transactional
    public String resetPasswordRequest(ResetPasswordRequest request) {
        String input = request.getInput();
        Users user;

        // Find user
        if (input.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD);
        } else if (input.matches(EMAIL_REGEX)) {
            user = getUserByEmail(input);
        } else {
            user = userRepository.findById(input).orElse(null);
        }

        if (user != null) {
            // Extract data
            String userId  = user.getId();
            String email = user.getEmail();

            // Generate otp - save otp
            String otp = generateOtpCodeForEmail(email, TokenScope.RESET_PASSWORD);

            // Send otp to mail
            String mailMessage = String.format(MailMessage.REQUEST_RESET_PASSWORD_MESSAGE, otp);
            mailServerService.sendMail(email, MailMessage.REQUEST_RESET_PASSWORD_SUBJECT, mailMessage);
            return generateResetPasswordToken(userId, TokenScope.RESET_PASSWORD, TokenStep.EMAIL_VERIFICATION);
        } else {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
    }

    /** Verify security information */

    @Transactional
    public String verifyCurrentPassword(VerifyCurrentPasswordRequest request, TokenScope tokenScope) {
        Users user = getUserBySecurityContextHolder();
        // Extract data
        String userId = user.getId();
        String email = user.getEmail();
        String password = request.getPassword();

        // Verify current password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        // Generate otp - save otp
        String otp = generateOtpCodeForEmail(email, tokenScope);

        // Send mail
        String mailMessage = String.format(MailMessage.VERIFY_EMAIL_UPDATE_MESSAGE, otp);
        mailServerService.sendMail(email, MailMessage.VERIFY_EMAIL_UPDATE_SUBJECT, mailMessage);

        return generateAccessTokenByScopeAndStep(userId, tokenScope, TokenStep.EMAIL_VERIFICATION);
    }

    @Transactional
    public String verifyEmailOtpCode(String token, TokenScope tokenScope, String otp) {
        Users user = getUserBySecurityContextHolder();
        String userId = user.getId();
        String email = user.getEmail();

        // Verify otp
        OtpCode otpCode = otpCodeRepository.findByEmailAndOtp(email, otp);

        if  (otpCode != null) {
            // Delete token after use
            deleteTokenAfterUse(token);

            // Delete otp
            otpCodeRepository.delete(otpCode);

            switch (tokenScope) {
                case CHANGE_PASSWORD -> {
                    return generateAccessTokenByScopeAndStep(userId, tokenScope, TokenStep.FINAL_STEP);
                }
                case CHANGE_EMAIL -> {
                    // Extract current step
                    TokenStep currentStepInToken = TokenStep.valueOf(extractClaimsFromToken(token, "step"));

                    // If current step is verify current email
                    if (currentStepInToken == TokenStep.EMAIL_VERIFICATION) {
                        // Next step is verify alternative email
                        return generateAccessTokenByScopeAndStep(userId, tokenScope, TokenStep.ALTERNATIVE_EMAIL_VERIFICATION);
                    } else if (currentStepInToken == TokenStep.ALTERNATIVE_EMAIL_CONFIRMED) {
                        email = extractClaimsFromToken(token, "email");
                        // Next step is call change email api
                        return generateChangeEmailToken(userId, email);
                    } else {
                        throw new AppException(ErrorCode.INVALID_TOKEN);
                    }
                }
                default -> throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        } else {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
    }

    @Transactional
    public String verifyForgetPasswordOtp(String token, String otp) {
        String userId = extractSubjectFromToken(token);
        Users users = userRepository.findById(userId).orElse(null);
        String email = null;
        if (users != null) {
            email = users.getEmail();
        }

        OtpCode currentOtpCode = otpCodeRepository.findByEmailAndOtp(email, otp);
        if (currentOtpCode != null) {
            // Delete otp after use
            otpCodeRepository.delete(currentOtpCode);

            // Delete token after use
            deleteTokenAfterUse(token);

            return generateAccessTokenByScopeAndStep(userId, TokenScope.RESET_PASSWORD, TokenStep.FINAL_STEP);

        } else {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken(), false);
        String jwtId = signToken.getJWTClaimsSet().getJWTID();

        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        TokenValid tokenValid = TokenValid.builder()
                .token(jwtId)
                .expiresAt(LocalDateTime.ofInstant(expiryTime.toInstant(), java.time.ZoneId.systemDefault()))
                .build();
        tokenValidRepository.save(tokenValid);
    }

    public LoginResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signJWT = verifyToken(request.getToken(), true);

        var jwtId = signJWT.getJWTClaimsSet().getJWTID();

        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();

        TokenExpired tokenExpired = TokenExpired.builder()
                .id(jwtId)
                .expiryTime(expiryTime)
                .build();
        tokenExpiredRepository.save(tokenExpired);

        var username = signJWT.getJWTClaimsSet().getSubject();

        Users user = userRepository.findById(username).orElse(null);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        String token = generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .build();
    }

    /** Util method */

    private Users getUserBySecurityContextHolder() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private Users getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    private String generateOtpCodeForEmail(String email, TokenScope scope) {
        // Check previous otp
        OtpCode otpCodeExist = otpCodeRepository.findByEmailAndScope(email, scope);

        // Delete previous otp have same scope if exist
        if (otpCodeExist != null) {
            otpCodeRepository.delete(otpCodeExist);
        }

        // Generate new otp
        String otpCode = RandomCode.generateOtp6Digits();

        OtpCode otp = OtpCode.builder()
                .email(email)
                .otp(otpCode)
                .scope(scope)
                .expiresAt(LocalDateTime.now().plusSeconds(TIMEOUT_NEXT_STEP_TOKEN))
                .build();
        otpCodeRepository.save(otp);

        return otpCode;
    }

    private String extractSubjectFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractClaimsFromToken(String token, String claim) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getClaim(claim).toString();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkTokenIsAllowToUse(String token) {
        boolean tokenIsAllowToUse = tokenValidationService.validateTokenUsage(token);
        if (!tokenIsAllowToUse) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }
    }

    public void deleteTokenAfterUse(String token) {
        TokenValid tokenValid = tokenValidRepository.findByToken(token);
        if (tokenValid != null) {
            tokenValidRepository.delete(tokenValid);
        }
    }

    public boolean validateTokenByScopeAndStep(String token, TokenScope scopeToCheck, TokenStep stepToCheck) throws JOSEException, ParseException {
        checkTokenIsAllowToUse(token);

        // Verify signature
        JWSVerifier verifier = new MACVerifier(JWT_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        boolean isSignatureValid = signedJWT.verify(verifier);

        if  (!isSignatureValid) {
            return false;
        }

        // Verify issue time
        Date current = new Date();
        Date tokenExpirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!current.before(tokenExpirationTime)) {
            return false;
        }

        // Verify token step
        TokenStep stepInToken = TokenStep.valueOf(extractClaimsFromToken(token, "step"));
        if (stepInToken != stepToCheck) {
            return false;
        }

        // Verify token scope
        TokenScope scopeInToken = TokenScope.valueOf(extractClaimsFromToken(token, "tokenScope"));
        return scopeInToken == scopeToCheck;

    }
}
