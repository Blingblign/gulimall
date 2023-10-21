package com.zzclearning.gulimall.ware.exception;

/**
 * @author bling
 * @create 2023-02-20 11:19
 */
public class StockNotEnoughException extends RuntimeException{
    static final long serialVersionUID = -79L;

    public StockNotEnoughException(String message) {
        super(message);
    }
}
