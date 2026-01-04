package com.cardano_lms.server.Exception;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.io.IOException;
import org.springframework.dao.DataIntegrityViolationException;


@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(Exception exception) {
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }



    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFound(NoResourceFoundException ex) {
        ApiResponse<Object> apiResponse = ApiResponse.<Object>builder()
                .code(ErrorCode.NOT_FOUND.getCode())
                .message("Resource not found: " + ex.getResourcePath())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";

        String message = String.format("Tham số '%s' có giá trị '%s' không đúng kiểu. Yêu cầu kiểu: %s",
                paramName, value, requiredType);

        ApiResponse<?> response = ApiResponse.<Object>builder()
                .code(ErrorCode.INVALID_ARGUMENT.getCode())
                .message(message)
                .build();

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Object>> handleIOException(IOException ex) {
        ApiResponse<Object> apiResponse = ApiResponse.<Object>builder()
                .code(ErrorCode.FILE_UPLOAD_FAILED.getCode())
                .message(ErrorCode.FILE_UPLOAD_FAILED.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        
        String message = "Lỗi lưu dữ liệu";
        String rootMsg = ex.getMostSpecificCause().getMessage();
        
        if (rootMsg != null) {
            if (rootMsg.contains("value too long")) {
                message = "Nội dung quá dài. Vui lòng rút ngắn nội dung bài giảng.";
            } else if (rootMsg.contains("duplicate key") || rootMsg.contains("unique constraint")) {
                message = "Dữ liệu đã tồn tại. Vui lòng kiểm tra lại.";
            } else if (rootMsg.contains("foreign key") || rootMsg.contains("violates foreign key")) {
                message = "Không thể thực hiện do liên kết dữ liệu.";
            } else if (rootMsg.contains("not-null") || rootMsg.contains("null value")) {
                message = "Thiếu thông tin bắt buộc.";
            }
        }
        
        ApiResponse<Object> apiResponse = ApiResponse.<Object>builder()
                .code(ErrorCode.DATABASE_ERROR.getCode())
                .message(message)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiResponse);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<Object> apiResponse = ApiResponse.<Object>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(apiResponse);
    }


    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;
        try {
            errorCode = ErrorCode.valueOf(enumKey);

            var constraintViolation =
                    exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);

            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
        } catch (IllegalArgumentException e) {

        }

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(
                Objects.nonNull(attributes)
                        ? mapAttribute(errorCode.getMessage(), attributes)
                        : errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ApiResponse<Object> apiResponse = ApiResponse.<Object>builder()
                .code(ErrorCode.MISSING_CREDENTIALS.getCode())
                .message(ErrorCode.MISSING_CREDENTIALS.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiResponse);
    }

}
