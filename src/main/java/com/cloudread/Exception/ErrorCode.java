package com.cloudread.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {
    // System errors (1000-1199)
    UNCATEGORIZED_ERROR(1000, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR(1001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(1002, "Database connection error", HttpStatus.INTERNAL_SERVER_ERROR),
    TIMEOUT_ERROR(1003, "Request timeout", HttpStatus.REQUEST_TIMEOUT),
    FILE_STORAGE_ERROR(1004, "File storage system error", HttpStatus.INTERNAL_SERVER_ERROR),
    CACHE_ERROR(1005, "Cache service error", HttpStatus.INTERNAL_SERVER_ERROR),
    MESSAGE_QUEUE_ERROR(1006, "Message queue error", HttpStatus.INTERNAL_SERVER_ERROR),
    ERROR_UPLOAD_FILE(1007, "Error uploading file", HttpStatus.INTERNAL_SERVER_ERROR),
    ERROR_DELETE_FILE(1008, "Error deleting file", HttpStatus.INTERNAL_SERVER_ERROR),

    // Authentication errors (1200-1399)
    UNAUTHENTICATED(1200, "Users not authenticated", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1201, "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1202, "Token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(1203, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(1204, "Account has been locked", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(1205, "Account has been disabled", HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_VERIFIED(1206, "Email not verified", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(1207, "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    SESSION_EXPIRED(1208, "Session has expired", HttpStatus.UNAUTHORIZED),
    INVALID_OTP(1209, "Invalid or expired OTP", HttpStatus.UNAUTHORIZED),
    WRONG_PASSWORD(1210, "Wrong password", HttpStatus.UNAUTHORIZED),

    // Authorization errors (1300-1399)
    UNAUTHORIZED(1300, "You do not have permission", HttpStatus.FORBIDDEN),
    INSUFFICIENT_PERMISSION(1301, "Insufficient permissions", HttpStatus.FORBIDDEN),
    ROLE_NOT_ALLOWED(1302, "Role not allowed for this action", HttpStatus.FORBIDDEN),
    RESOURCE_ACCESS_DENIED(1303, "Access denied to this resource", HttpStatus.FORBIDDEN),

    // Not found errors (1400-1599)
    NOT_FOUND(1400, "Resource not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1401, "Users not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(1402, "Product not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1403, "Category not found", HttpStatus.NOT_FOUND),
    FILE_NOT_FOUND(1404, "File not found", HttpStatus.NOT_FOUND),
    AUTHOR_NOT_FOUND(1405, "Author not found", HttpStatus.NOT_FOUND),

    // Validation errors (1500-1599)
    INVALID_INPUT(1500, "Invalid input data", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD(1501, "Missing required field", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(1502, "Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT(1503, "Invalid phone format", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT(1504, "Invalid date format", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_WEAK(1505, "Password does not meet requirements", HttpStatus.BAD_REQUEST),
    PASSWORD_DID_NOT_MATCH(1506, "Password does not match", HttpStatus.BAD_REQUEST),
    FIELD_TOO_LONG(1507, "Field exceeds maximum length", HttpStatus.BAD_REQUEST),
    FIELD_TOO_SHORT(1508, "Field is below minimum length", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(1509, "Invalid file type", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(1510, "File size exceeded limit", HttpStatus.BAD_REQUEST),
    MISSING_IMAGE(1511, "Missing image", HttpStatus.BAD_REQUEST),
    MISSING_DOCUMENT(1512, "Missing document", HttpStatus.BAD_REQUEST),
    USERNAME_NOT_ALLOWED(1513, "Username not allowed", HttpStatus.BAD_REQUEST),

    // Logic errors (1600-1799)


    // Conflict errors (1800-1999)
    CONFLICT(1800, "Resource conflict", HttpStatus.CONFLICT),
    USER_ALREADY_EXISTS(1801, "Users already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(1802, "Email already registered", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS(1803, "Username already taken", HttpStatus.CONFLICT),
    DUPLICATE_ENTRY(1804, "Duplicate entry detected", HttpStatus.CONFLICT),
    RESOURCE_ALREADY_EXISTS(1805, "Resource already exists", HttpStatus.CONFLICT),
    AUTHOR_ALREADY_EXISTS(1806, "Author already exists", HttpStatus.CONFLICT),
    CATEGORY_ALREADY_EXISTS(1807, "Category already exists", HttpStatus.CONFLICT),

    // VNPAY
    VNPAY_PAYMENT_ERROR(2000,"Vnpay Error", HttpStatus.INTERNAL_SERVER_ERROR),

    ;

    int code;
    String message;
    HttpStatus status;
}
