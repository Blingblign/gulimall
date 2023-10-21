package com.zzclearning.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物项-->订单项
 * @author bling
 * @create 2023-02-17 17:19
 */
@Data
public class OrderItem {
    private Long         skuId;
    private String       image;//商品图片
    private String       title;//商品名称
    private List<String> skuAttrValues;//商品销售属性
    private BigDecimal   price;//商品单价
    private   Integer      count;//商品数量
    private BigDecimal   totalPrice;//商品总价
    private BigDecimal weight = new BigDecimal("0.0"); //商品重量
}
