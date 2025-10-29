package com.cloudread.Exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
public class AppException extends RuntimeException {
    private List<ErrorCode> errorList;
    private HttpStatus httpStatus;

    public AppException(List<ErrorCode> errorList) {
        this.errorList = errorList;
        this.httpStatus = determineHttpStatus(errorList);
    }

    public AppException(ErrorCode errorCode) {
        this.errorList = List.of(errorCode);
        this.httpStatus = errorCode.getStatus();
    }

    /**
     * 1. 401/403 (Authentication/Authorization)
     * 2. 404 (Not Found)
     * 3. 400/422 (Bad Request/Validation)
     * 4. 409 (Conflict)
     * 5. 5xx (Server Errors)
     */
    private HttpStatus determineHttpStatus(List<ErrorCode> errors) {
        if (errors == null || errors.isEmpty()) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        boolean has401or403 = false;
        boolean has404 = false;
        boolean has400 = false;
        boolean has409 = false;
        boolean has5xx = false;

        for (ErrorCode error : errors) {
            HttpStatus status = error.getStatus();
            
            if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                has401or403 = true;
            } else if (status == HttpStatus.NOT_FOUND) {
                has404 = true;
            } else if (status == HttpStatus.BAD_REQUEST || status == HttpStatus.UNPROCESSABLE_ENTITY) {
                has400 = true;
            } else if (status == HttpStatus.CONFLICT) {
                has409 = true;
            } else if (status.is5xxServerError()) {
                has5xx = true;
            }
        }

        if (has401or403) {

            return errors.stream()
                    .map(ErrorCode::getStatus)
                    .filter(s -> s == HttpStatus.UNAUTHORIZED || s == HttpStatus.FORBIDDEN)
                    .findFirst()
                    .orElse(HttpStatus.UNAUTHORIZED);
        }
        if (has404) return HttpStatus.NOT_FOUND;
        if (has400) return HttpStatus.BAD_REQUEST;
        if (has409) return HttpStatus.CONFLICT;
        if (has5xx) return HttpStatus.INTERNAL_SERVER_ERROR;

        // Fallback: lấy status từ error đầu tiên
        return errors.get(0).getStatus();
    }
}
