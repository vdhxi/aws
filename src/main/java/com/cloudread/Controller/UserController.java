package com.cloudread.Controller;

import com.cloudread.DTO.Request.Authenticate.ChangePasswordRequest;
import com.cloudread.DTO.Request.Authenticate.OtpRequest;
import com.cloudread.DTO.Request.Authenticate.VerifyCurrentPasswordRequest;
import com.cloudread.DTO.Request.User.EmailRequest;
import com.cloudread.DTO.Request.User.UserUpdateRequest;
import com.cloudread.DTO.Response.ApiResponse;
import com.cloudread.DTO.Response.User.UserResponse;
import com.cloudread.Enum.TokenScope;
import com.cloudread.Enum.TokenStep;
import com.cloudread.Exception.AppException;
import com.cloudread.Exception.ErrorCode;
import com.cloudread.Service.Impl.AuthenticateService;
import com.cloudread.Service.Impl.UserServiceImpl;
import com.cloudread.Service.UserService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    UserService userService;
    AuthenticateService authenticateService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        var result = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<UserResponse>>builder()
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        var result = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<UserResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PutMapping()
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@RequestBody @Valid UserUpdateRequest request) {
        var result = userService.updateUser(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<UserResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PostMapping("/change-password/request")
    public ResponseEntity<ApiResponse<Object>> changePasswordRequest() {
        var result = authenticateService.generateRequestTokenByScope(TokenScope.CHANGE_PASSWORD);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .data(result)
                        .build()
        );
    }

    @PostMapping("/change-email/request")
    public ResponseEntity<ApiResponse<Object>> changeEmailRequest() {
        var result = authenticateService.generateRequestTokenByScope(TokenScope.CHANGE_EMAIL);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .data(result)
                        .build()
        );
    }

    @PostMapping("/verify-password")
    public ResponseEntity<ApiResponse<String>> verifyPassword(@RequestHeader ("Access-Token") String accessToken,
                                                              @RequestBody @Valid VerifyCurrentPasswordRequest request) {
        String scopeInToken = extractScopeFromToken(accessToken);
        TokenScope scope = TokenScope.valueOf(scopeInToken);

        var result = authenticateService.verifyCurrentPassword(request, scope);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<String>builder()
                        .data(result)
                        .build()
        );
    }

    @PostMapping("/verify-email-otp")
    public ResponseEntity<ApiResponse<Object>> verifyEmailOtp(@RequestHeader ("Access-Token") String accessToken,
                                                              @RequestBody @Valid OtpRequest request) throws JOSEException, ParseException {
        String scopeInToken = extractScopeFromToken(accessToken);
        TokenScope scope = TokenScope.valueOf(scopeInToken);

        boolean isAllow = authenticateService.validateTokenByScopeAndStep(accessToken, scope, TokenStep.EMAIL_VERIFICATION);

        if (isAllow) {
            var result = authenticateService.verifyEmailOtpCode(accessToken, scope, request.getOtp());
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.builder()
                            .data(result)
                            .build()
            );
        } else {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    @PostMapping("/change-email/verify-new-email")
    public ResponseEntity<ApiResponse<Object>> changeEmailValidateNewEmail(@RequestHeader ("Access-Token") String accessToken,
                                                                           @RequestBody @Valid EmailRequest request) throws JOSEException, ParseException {
        String scopeInToken = extractScopeFromToken(accessToken);
        TokenScope scope = TokenScope.valueOf(scopeInToken);
        boolean isAllow = authenticateService.validateTokenByScopeAndStep(accessToken, scope, TokenStep.ALTERNATIVE_EMAIL_VERIFICATION);
        if (isAllow) {
            var result = authenticateService.verifyEmailExist(request.getEmail(), scope);
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.builder()
                            .data(result)
                            .build()
            );
        } else  {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    @PostMapping("/change-email/verify-new-email/otp")
    public ResponseEntity<ApiResponse<Object>> changeEmailValidateNewEmail(@RequestHeader ("Access-Token") String accessToken,
                                                                           @RequestBody @Valid OtpRequest request) throws JOSEException, ParseException {
        String scopeInToken = extractScopeFromToken(accessToken);
        TokenScope scope = TokenScope.valueOf(scopeInToken);
        boolean isAllow = authenticateService.validateTokenByScopeAndStep(accessToken, scope, TokenStep.ALTERNATIVE_EMAIL_CONFIRMED);
        if (isAllow) {
            var result = authenticateService.verifyEmailOtpCode(accessToken, scope, request.getOtp());
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.builder()
                            .data(result)
                            .build()
            );
        } else {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(@RequestHeader ("Access-Token")  String accessToken,
                                                                    @RequestBody @Valid ChangePasswordRequest request) throws ParseException, JOSEException {
        String scopeInToken = extractScopeFromToken(accessToken);
        TokenScope scope = TokenScope.valueOf(scopeInToken);
        boolean isAllow;
        if (scope == TokenScope.CHANGE_PASSWORD) {
            isAllow = authenticateService.validateTokenByScopeAndStep(accessToken, TokenScope.CHANGE_PASSWORD, TokenStep.FINAL_STEP);
        } else if (scope == TokenScope.RESET_PASSWORD) {
            isAllow = authenticateService.validateTokenByScopeAndStep(accessToken, TokenScope.RESET_PASSWORD, TokenStep.FINAL_STEP);
        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (!isAllow) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        } else {
            userService.changePassword(accessToken, request);
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.builder()
                            .data("Password changed")
                            .build()
            );
        }
    }

    @PutMapping("/change-email")
    public ResponseEntity<ApiResponse<UserResponse>> changeEmail(@RequestHeader ("Access-Token")  String accessToken) throws ParseException, JOSEException {
        boolean isAllow = authenticateService.validateTokenByScopeAndStep(accessToken, TokenScope.CHANGE_EMAIL, TokenStep.FINAL_STEP);
        if (!isAllow) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        } else {
            var result = userService.changeEmail(accessToken);
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.<UserResponse>builder()
                            .data(result)
                            .build()
            );
        }
    }



    String extractScopeFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getClaim("scope").toString();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /* Change password flow:
     * 1. User requests to change password - @PostMapping("/change-password/request")
     * 2. User verifies current password using the token - @PostMapping("/verify-password")
     * 3. User verifies email otp using the token - @PostMapping("/verify-email-otp")
     * 4. User changes password using the token - @PutMapping("/change-password")
     * Change email flow:
     * 1. User requests to change email - @PostMapping("/change-email/request")
     * 2. User verifies current password using the token - @PostMapping("/verify-password")
     * 3. User verifies email otp using the token - @PostMapping("/verify-email-otp")
     * 4. User verifies new email to check if it is available - @PostMapping("/change-email/verify-new-email")
     * 5. User verifies otp sent to new email - @PostMapping("/change-email/verify-new-email/otp")
     * 6. User changes email using the token - @PutMapping("/change-email")
     * */



}
