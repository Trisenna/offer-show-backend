package com.offershow.exception;

import com.offershow.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleBusinessException(BusinessException e) {
        log.error("Business exception: {}", e.getMessage());
        return ResponseResult.error(400, e.getMessage());
    }

    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseResult<Object> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("Resource not found: {}", e.getMessage());
        return ResponseResult.error(404, e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Method argument validation failed: {}", e.getMessage());
        return handleBindingResult(e.getBindingResult());
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleBindException(BindException e) {
        log.error("Parameter binding failed: {}", e.getMessage());
        return handleBindingResult(e.getBindingResult());
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Constraint violation: {}", e.getMessage());
        List<Map<String, String>> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            Map<String, String> error = new HashMap<>();
            error.put("field", violation.getPropertyPath().toString());
            error.put("message", violation.getMessage());
            errors.add(error);
        }

        String errorMsg = errors.stream()
                .map(map -> map.get("field") + ": " + map.get("message"))
                .collect(Collectors.joining(", "));

        return ResponseResult.error(400, "参数验证失败: " + errorMsg);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseResult<Object> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseResult.error(500, "服务器内部错误: " + e.getMessage());
    }

    /**
     * 处理绑定结果
     */
    private ResponseResult<Object> handleBindingResult(BindingResult bindingResult) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            Map<String, String> error = new HashMap<>();
            error.put("field", fieldError.getField());
            error.put("message", fieldError.getDefaultMessage());
            errors.add(error);
        }

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("errors", errors);

        String errorMsg = errors.stream()
                .map(map -> map.get("field") + ": " + map.get("message"))
                .collect(Collectors.joining(", "));

        return ResponseResult.paramError("参数验证失败: " + errorMsg);
    }
}