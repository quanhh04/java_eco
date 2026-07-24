package com.demo.securities.spring;

import com.demo.securities.exception.DuplicateException;
import com.demo.securities.exception.NotFoundException;
import com.demo.securities.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Idiom Spring cho xử lý lỗi tập trung — 1 class, vài method, so với
 * Router.invoke (Java SE thuần), BaseApiServlet.handle (Servlet) hay
 * 4 file *ExceptionMapper riêng lẻ (JAX-RS) thì gọn hơn hẳn.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({ValidationException.class, DuplicateException.class, IllegalArgumentException.class,
            DateTimeParseException.class, NullPointerException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", String.valueOf(e.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Loi he thong: " + e.getMessage()));
    }
}
