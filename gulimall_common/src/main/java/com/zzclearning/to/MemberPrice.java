package com.zzclearning.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 添加商品功能：会员价格
 * @author bling
 * @create 2022-11-02 12:05
 */
@Data
public class MemberPrice implements Serializable {
    private Long id;
    private String name;
    private BigDecimal price;
    private Long skuId;
}
