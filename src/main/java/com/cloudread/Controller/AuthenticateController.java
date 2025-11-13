package com.cloudread.Controller;

import com.cloudread.DTO.Request.Authenticate.*;
import com.cloudread.DTO.Request.User.EmailRequest;
import com.cloudread.DTO.Request.User.UserCreateRequest;
import com.cloudread.DTO.Response.ApiResponse;
import com.cloudread.DTO.Response.Authenticate.IntrospectResponse;
import com.cloudread.DTO.Response.Authenticate.LoginResponse;
import com.cloudread.DTO.Response.User.UserResponse;
import com.cloudread.Enum.TokenScope;
import com.cloudread.Enum.TokenStep;
import com.cloudread.Exception.AppException;
import com.cloudread.Exception.ErrorCode;
import com.cloudread.Service.Impl.AuthenticateService;
import com.cloudread.Service.Impl.UserServiceImpl;
import com.cloudread.Service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticateController {
    AuthenticateService authenticateService;
    UserService userService;

    /** Register */
    // Step 1
    @PostMapping("/register/verify-email-exist")
    public ResponseEntity<ApiResponse<Object>> validateEmailExist(@RequestBody @Valid EmailRequest request) {
        var result = authenticateService.verifyEmailExist(request.getEmail(), TokenScope.REGISTER);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .data(result)
                        .build()
        );
    }

    // Step 2
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestHeader ("Access-Token") String accessToken,
                                                                @RequestBody @Valid UserCreateRequest request) throws ParseException, JOSEException {
        boolean isAllow = authenticateService.validateTokenByScopeAndStep(accessToken, TokenScope.REGISTER, TokenStep.REGISTER_STEP);
        if (isAllow) {
            var result = userService.createUser(request, accessToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<UserResponse>builder()
                            .data(result)
                            .build()
            );
        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        var result = authenticateService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<LoginResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request) {
        var result = authenticateService.introspect(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<IntrospectResponse>builder()
                        .data(result)
                        .build()
        );
    }

    /** Reset password */
    // Step 1
    @PostMapping("/forget-password")
    public ResponseEntity<ApiResponse<Object>> forgetPasswordRequest(@RequestBody ResetPasswordRequest request) {
        var result = authenticateService.resetPasswordRequest(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .data(result)
                        .build()
        );
    }

    // Step 2
    @PostMapping("/forget-password/verify-otp")
    public ResponseEntity<ApiResponse<Object>> forgetPasswordVerifyOtp(@RequestHeader ("Access-Token") String accessToken,
                                                                       @RequestBody @Valid OtpRequest request) throws JOSEException, ParseException {
        boolean isAllow = authenticateService.validateTokenByScopeAndStep(accessToken, TokenScope.RESET_PASSWORD, TokenStep.EMAIL_VERIFICATION);
        if (isAllow) {
            var result = authenticateService.verifyForgetPasswordOtp(accessToken, request.getOtp());
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.builder()
                            .data(result)
                            .build()
            );
        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
    // Step 3
    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@RequestHeader ("Access-Token") String accessToken,
                                                             @RequestBody @Valid ChangePasswordRequest request) throws JOSEException, ParseException {

        boolean isAllow = authenticateService.validateTokenByScopeAndStep(accessToken, TokenScope.RESET_PASSWORD, TokenStep.FINAL_STEP);
        if (isAllow) {
            userService.changePassword(accessToken, request);
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.builder()
                            .message("Password reset successfully")
                            .build()
            );
        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public  ResponseEntity<ApiResponse<Object>> logout(@RequestBody LogoutRequest request) throws JOSEException, ParseException {
        authenticateService.logout(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        var result = authenticateService.refreshToken(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<LoginResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PostMapping("/ping")
    public ResponseEntity<ApiResponse<Object>> pingTest() {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .message("Pong!")
                        .build()
        );
    }

}
