package com.zzclearning.gulimall.auth.exception;

/**
 * @author bling
 * @create 2023-02-09 19:29
 */
public class CodeIncorrectException extends RuntimeException{
    static final long serialVersionUID = 1L;

    public CodeIncorrectException(String message) {
        super(message);
    }
}
