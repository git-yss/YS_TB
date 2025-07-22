package org.ys.commens.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ys.commens.pojo.CommentResult;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public CommentResult handleRuntimeException(RuntimeException ex) {
        return new CommentResult().error(ex.getMessage());
    }
}