package com.zzclearning.gulimall.ware.constant;

/**
 * @author bling
 * @create 2023-02-20 16:42
 */
public enum StockLockStatus {
    STOCK_LOCKED(1,"已锁定"),STOCK_RELEASED(2,"已解锁"),STOCK_DEDUCED(3,"已扣减");
    private final int code;
    private final String msg;

    StockLockStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
