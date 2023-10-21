package com.zzclearning.common.exception;



/**
 * 自定义异常错误码和错误信息
 * 错误码列表：
 * 10： 通用
 *
 * 11：商品
 * 12：订单
 * 13：购物车
 * 14：物流
 * 15：用户
 * 21: 库存
 * @author bling
 * @create 2022-10-27 10:11
 */
public enum BizExceptionEnum {
    VALIDATION_EXCEPTION(10001,"参数格式校验异常"),

    UNKNOWN_EXCEPTION(10000,"全局未知异常"),
    PHONE_INVALID_EXCEPTION(15001,"手机号格式错误"),
    CODE_QUERY_MUCH_EXCEPTION(15002,"验证码查询过于频繁"),
    USER_PHONE_EXIST_EXCEPTION(15004, "该手机号已注册"),
    USER_NAME_EXIST_EXCEPTION(15003, "用户名已存在"),
    USER_INVALID_EXCEPTION(15005, "用户名或密码错误"),
    NO_STOCK_EXCEPTION(21001,"商品库存不足"),

    PRODUCT_UP_EXCEPTION(11000,"商品上架异常");


    private final Integer code;
    private final String message;

    BizExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
