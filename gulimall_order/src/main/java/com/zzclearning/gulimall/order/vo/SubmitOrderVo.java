package com.zzclearning.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 接受订单提交表单的数据
 * @author bling
 * @create 2023-02-17 18:14
 */
@Data
public class SubmitOrderVo {
    private Long addrId;
    private BigDecimal payPrice;
    private String uniqueToken;
}
