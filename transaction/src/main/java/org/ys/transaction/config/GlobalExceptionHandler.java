package org.ys.transaction.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ys.commens.pojo.CommentResult;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public CommentResult handleRuntimeException(RuntimeException ex) {
        return new CommentResult().error(ex.getMessage());
    }
}