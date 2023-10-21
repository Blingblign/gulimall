package com.zzclearning.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 添加商品功能：会员金币和积分
 * @author bling
 * @create 2022-11-02 12:07
 */
@Data
public class Bounds {
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
