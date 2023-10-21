package com.zzclearning.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author bling
 * @create 2023-02-23 11:39
 */
@Data
public class PayVo {
    private String orderSn;//商户订单号，商户网站订单系统中唯一订单号，必填
    private String totalAmount;//付款金额，必填
    private String subject;//订单名称，必填
    private String body;//商品描述，可空
    private String timeExpire;//订单超时绝对时间
}
