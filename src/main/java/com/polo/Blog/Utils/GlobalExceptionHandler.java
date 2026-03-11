package com.polo.Blog.Utils;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 单独处理静态资源找不到的情况 (Spring 6+)
     * 避免控制台打印大量堆栈信息
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFoundException(NoResourceFoundException e) {
        // 什么都不做，或者只打印一个简单的 debug 日志
        log.debug("静态资源不存在: {}", e.getResourcePath());
        // 如果你需要返回 404 给前端，可以在这里处理，或者直接抛出让 Spring 默认处理
    }

    /**
     * 处理系统未知异常
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e) { // 假设返回 String 或 Result 对象

        // 1. 如果是空指针，e.getMessage() 是 null，这里加个判断让日志更清晰
        String message = e.getMessage();
        if (message == null) {
            message = e.toString(); // 这样会打印 "java.lang.NullPointerException" 而不是 "null"
        }

        // 2. 关键！一定要把 e (异常对象) 传给 logger，这样才能看到堆栈信息
        log.error("系统未知错误: {}", message, e);

        return "系统内部错误，请联系管理员"; // 返回给前端的友好提示
    }
}
