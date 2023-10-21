package com.zzclearning.common.constant;

import lombok.Data;

/**
 * @author bling
 * @create 2023-02-20 10:38
 */
public enum OrderStatusEnum {
    ORDER_NEW(0,"待付款"),
    ORDER_READY(1,"待发货"),
    ORDER_DELIVERY(2,"已发货"),
    ORDER_FINISHED(3,"已完成"),
    ORDER_CLOSED(4,"已关闭"),
    ORDER_INVALID(5,"无效订单");
    private final Integer status;
    private final String msg;

    OrderStatusEnum(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
