package com.zzclearning.gulimall.member.exception;

/**
 * @author bling
 * @create 2023-02-10 9:32
 */
public class UserNameExistException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public UserNameExistException(String message) {
        super(message);
    }
}
