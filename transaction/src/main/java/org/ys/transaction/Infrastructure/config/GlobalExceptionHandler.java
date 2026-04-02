package org.ys.transaction.Infrastructure.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ys.transaction.Interface.VO.CommentResult;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public CommentResult handleRuntimeException(RuntimeException ex) {
        return CommentResult.error(ex.getMessage() != null ? ex.getMessage() : "服务器异常");
    }
}