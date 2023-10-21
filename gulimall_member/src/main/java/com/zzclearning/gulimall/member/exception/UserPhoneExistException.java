package com.zzclearning.gulimall.member.exception;

/**
 * @author bling
 * @create 2023-02-10 9:31
 */
public class UserPhoneExistException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public UserPhoneExistException(String message) {
        super(message);
    }
}
